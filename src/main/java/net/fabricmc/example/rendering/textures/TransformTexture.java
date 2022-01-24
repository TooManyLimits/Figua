package net.fabricmc.example.rendering.textures;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.example.math.MathUtils;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL32.*;

/**
 * A texture which holds data about the transforms of an AvatarState.
 */
public class TransformTexture {

    //The maximum size of a transform texture is 256x256, translating to 16384 parts in your model.
    //Groups don't count towards this limit, meaning you'd have to be truly insane to break it with normal usage.
    public static final int MAX_SIZE = 256;
    //The OpenGL handle for this texture object
    private final int handle;
    //A privately maintained buffer for copying data to the texture.
    private final FloatBuffer copyBuffer;

    public TransformTexture(int numTransforms) {
        //We need 4 pixels per transform we're storing, since each pixel is 4 floats. 4 pixels == 16 floats == 1 matrix
        //Did power of 2 because I felt like it, probably not a noticeable difference but w/e
        int numPixels = MathUtils.nextPowerOfTwo(numTransforms * 4);
        if (numPixels > MAX_SIZE*MAX_SIZE) {
            throw new IllegalStateException("Bro how do u have over " + MAX_SIZE*MAX_SIZE/4 + " model parts what r u doing");
        }
        int width = Math.min(MAX_SIZE, numPixels);
        int height = Math.max(numPixels / MAX_SIZE, 1);
        copyBuffer = BufferUtils.createFloatBuffer(width * 4);

        //Generate the texture with openGL
        handle = GlStateManager._genTexture();
        GlStateManager._bindTexture(handle);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //GlStateManager wants an IntBuffer down here for some reason
        FloatBuffer data = createDefaultFloatBuffer(numPixels * 4);

        //Set the UNPACK_ALIGNMENT since it breaks without doing this apparently
        GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, data);
        GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, 4);

        GlStateManager._bindTexture(0);
    }

    public void bind(int textureUnit) {
        GlStateManager._activeTexture(GL_TEXTURE0 + textureUnit);
        GlStateManager._bindTexture(handle);
    }

    public void close() {
        GlStateManager._deleteTexture(handle);
    }

    /**
     * Uploads a buffer of vertex data into the texture.
     * Can upload multiple contiguous matrices at once.
     * @param firstIndex The index of the first matrix to be uploaded.
     * @param lastIndex The index of the second matrix to be uploaded.
     * @param buf The buffer containing the data to be uploaded.
     */
    public void uploadData(int firstIndex, int lastIndex, FloatBuffer buf) {
        int maxSizeInMatrices = MAX_SIZE / 4; //each matrix is 4 pixels
        bind(1);
        int startCopy = firstIndex;
        //Set the GL unpacking alignment to 1 while uploading textures, since our data is contiguous
        GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, 1);
        //Iterate over the texture one row at a time, to upload contiguous data segments
        while (startCopy <= lastIndex) {
            int endCopy = Math.min(lastIndex, (firstIndex / maxSizeInMatrices + 1) * maxSizeInMatrices - 1);
            int copyWidth = endCopy - startCopy + 1; //Number of matrices we're filling
            //Clear the buffer since last time we used it
            copyBuffer.clear();
            //Put the amount we're copying into the buffer
            copyBuffer.put(0, buf, (startCopy - firstIndex) * 16, copyWidth * 16);
            //Set position manually, since the above "put" method doesn't modify it
            copyBuffer.position(copyWidth * 16);
            //Flip buffer before read
            copyBuffer.flip();
            int x = startCopy % maxSizeInMatrices;
            int y = startCopy / maxSizeInMatrices;
            //GlStateManager version only supports a long pointer for some reason
            glTexSubImage2D(GL_TEXTURE_2D, 0, x * 4, y, copyWidth * 4, 1, GL_RGBA, GL_FLOAT, copyBuffer);
            startCopy = endCopy + 1;
        }
        //Reset the unpack alignment back to the default of 4
        GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, 4);
    }

    /**
     * Creates a FloatBuffer completely filled with copies of the identity matrix
     */
    private static FloatBuffer createDefaultFloatBuffer(int numFloats) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(numFloats);
        for (int i = 0; i < numFloats; i++)
            buffer.put(i % 16 % 5 == 0 ? 1 : 0);
        buffer.flip();
        return buffer;
    }

}
