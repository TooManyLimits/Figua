package net.fabricmc.example.rendering;

import net.fabricmc.example.FiguaMod;
import net.fabricmc.example.math.Matrix4;
import net.fabricmc.example.mixin.GameRendererAccessor;
import net.fabricmc.example.rendering.shader.FiguaShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL32.*;

/**
 * Various handy functions related to rendering.
 */
public class RenderUtils {

    private static VertexLayout defaultVertexLayout;
    private static FiguaShader defaultFiguaShader;

    /**
     * Gets the default figua vertex layout.
     * Lazily calculates when first needed, then caches the result.
     */
    public static VertexLayout defaultVertexLayout() {
        if (defaultVertexLayout == null) {
            defaultVertexLayout = new VertexLayout()
                    .attribute(GL_FLOAT, 3, false, false) //Position
                    .attribute(GL_UNSIGNED_BYTE, 4, true, false) //Color
                    .attribute(GL_FLOAT, 2, false, false) //Texture UV
                    .attribute(GL_FLOAT, 3, false, false) //Normal
                    .attribute(GL_SHORT, 1, false, true); //Transform index
        }
        return defaultVertexLayout;
    }

    /**
     * Gets the default figua shader.
     * Lazily calculates when first needed, then caches the result.
     * @return
     */
    public static FiguaShader defaultFiguaShader() {
        if (defaultFiguaShader == null) {
            try {
                Path path = FiguaMod.getAssetPath().resolve("shaders");
                defaultFiguaShader = FiguaShader.fromResources(path, "default", false);
                defaultFiguaShader.setupTextureUnitBinding("MainTexture", 0);
                defaultFiguaShader.setupTextureUnitBinding("TransformTexture", 1);
                //defaultFiguaShader.setUniform("ModelViewMat", Matrix4.rotateY(Math.toRadians(45)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultFiguaShader;
    }

    /**
     * Uses the accessor to get the current minecraft FOV.
     */
    public static double getCurrentFov() {
        MinecraftClient client = MinecraftClient.getInstance();
        GameRenderer gameRenderer = client.gameRenderer;
        return ((GameRendererAccessor) gameRenderer).figua$getFov(gameRenderer.getCamera(), client.getTickDelta(), true);
    }

    /**
     * Gets a minecraft projection matrix with the current fov.
     * Caches result for future calls. Invalidated if fov changes.
     */
    private static Matrix4 projMat;
    private static double lastFov;

    public static Matrix4 getMCProjectionMatrix() {
        if (getCurrentFov() != lastFov) {
            lastFov = getCurrentFov();
            projMat = Matrix4.fromMatrix4f(MinecraftClient.getInstance().gameRenderer.getBasicProjectionMatrix(lastFov));
        }
        return projMat;
    }
}
