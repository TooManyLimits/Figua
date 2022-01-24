package net.fabricmc.example.rendering.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.example.FiguaMod;
import org.lwjgl.opengl.GL32;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a shader program for use with figua.
 */
public class FiguaShader {

    private final int handle;
    private final HashMap<String, Integer> uniformLocations;

    /**
     * Creates a new FiguaShader given the inputs
     * These inputs are the source text of the shader.
     * If geom is null, the shader will not have a geometry step.
     * TODO: Implement geometry shaders (yeah right, like I'll get around to that)
     */
    private FiguaShader(String vertSource, String geomSource, String fragSource) {

        //Create and compile vertex shader
        int vertexShaderHandle = GlStateManager.glCreateShader(GL32.GL_VERTEX_SHADER);
        GlStateManager.glShaderSource(vertexShaderHandle, List.of(vertSource));
        GlStateManager.glCompileShader(vertexShaderHandle);
        checkForCompilationError(vertexShaderHandle);

        //Create and compile geometry shader, if it exists
        int geometryShaderHandle = 0;
        if (geomSource != null) {
            geometryShaderHandle = GlStateManager.glCreateShader(GL32.GL_GEOMETRY_SHADER);
            GlStateManager.glShaderSource(geometryShaderHandle, List.of(geomSource));
            GlStateManager.glCompileShader(geometryShaderHandle);
            checkForCompilationError(geometryShaderHandle);
        }

        //Create adn compile fragment shader
        int fragmentShaderHandle = GlStateManager.glCreateShader(GL32.GL_FRAGMENT_SHADER);
        GlStateManager.glShaderSource(fragmentShaderHandle, List.of(fragSource));
        GlStateManager.glCompileShader(fragmentShaderHandle);
        checkForCompilationError(fragmentShaderHandle);

        //Create program
        handle = GlStateManager.glCreateProgram();

        //Attach the shaders
        GlStateManager.glAttachShader(handle, vertexShaderHandle);
        if (geomSource != null)
            GlStateManager.glAttachShader(handle, geometryShaderHandle);
        GlStateManager.glAttachShader(handle, fragmentShaderHandle);

        //Link the program
        GlStateManager.glLinkProgram(handle);
        checkForLinkingError(handle);

        //Now that we got the linked program, we can delete the individual components of it
        GlStateManager.glDeleteShader(vertexShaderHandle);
        if (geomSource != null)
            GlStateManager.glDeleteShader(geometryShaderHandle);
        GlStateManager.glDeleteShader(fragmentShaderHandle);

        uniformLocations = new HashMap<>();
    }

    /**
     * Checks if there was a compilation error in the shader, and prints it if there was.
     * @param shaderChunkHandle The handle of the shader to check for errors in
     */
    private static void checkForCompilationError(int shaderChunkHandle) {
        int success = GlStateManager.glGetShaderi(shaderChunkHandle, GL32.GL_COMPILE_STATUS);
        if (success == 0) {
            String error = GlStateManager.glGetShaderInfoLog(shaderChunkHandle, 32768).trim();
            FiguaMod.LOGGER.warn("Shader compilation error:");
            FiguaMod.LOGGER.warn(error);
        }
    }

    /**
     * Checks if there was a linking error in the shader program, and prints it if there was.
     * @param shaderProgramHandle The handle of the shader program to check for errors in
     */
    private static void checkForLinkingError(int shaderProgramHandle) {
        int success = GlStateManager.glGetProgrami(shaderProgramHandle, GL32.GL_LINK_STATUS);
        if (success == 0) {
            String error = GlStateManager.glGetProgramInfoLog(shaderProgramHandle, 32768).trim();
            FiguaMod.LOGGER.warn("Shader linking error:");
            FiguaMod.LOGGER.warn(error);
        }
    }

    public void use() {
        GlStateManager._glUseProgram(handle);
    }

    public void setUniform(String name, UniformConvertible value) {
        Integer loc = uniformLocations.get(name);
        if (loc == null) {
            loc = GlStateManager._glGetUniformLocation(handle, name);
            uniformLocations.put(name, loc);
        }
        use();
        value.uploadUniform(loc);
    }

    public void setupTextureUnitBinding(String name, int unit) {
        setUniform(name, (loc)->GlStateManager._glUniform1i(loc, unit));
    }

    /**
     * Deletes this program from the GPU. Call when you know you don't need it anymore.
     */
    public void close() {
        GlStateManager.glDeleteProgram(handle);
    }

    /**
     * Creates and returns a new FiguaShader based on shaders stored in a path
     * @param folderPath The path to the folder containing the shader files
     * @param name The name of the shader files, excluding suffixes (.vert, .frag, .geom)
     * @param includeGeometry Whether this should use a geometry shader or not
     * @return A new FiguaShader constructed from the resources given
     */
    public static FiguaShader fromResources(Path folderPath, String name, boolean includeGeometry) throws IOException {
        String vertSource = Files.readString(folderPath.resolve(name+".vert"));
        String geomSource = null;
        if (includeGeometry)
            geomSource = Files.readString(folderPath.resolve(name+".geom"));
        String fragSource = Files.readString(folderPath.resolve(name+".frag"));
        return new FiguaShader(vertSource, geomSource, fragSource);
    }

}
