package net.fabricmc.example.rendering.textures;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32.*;

/**
 * A texture that's actually used for rendering by figua.
 */
public class FiguaTexture {

    /**
     * The OpenGL handle of this texture.
     */
    private final int handle;

    public FiguaTexture(InputStream inputStream) {
        ByteBuffer imageData = null;
        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        try {
            imageData = TextureUtil.readResource(inputStream);
            imageData.rewind();
            imageData = STBImage.stbi_load_from_memory(imageData, width, height, channels, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now we have the data in a ByteBuffer, and we know the width and height.

        //Generate the texture with openGL:
        handle = GlStateManager._genTexture();
        GlStateManager._bindTexture(handle);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //GlStateManager wants an IntBuffer down here for some reason
        //Send the data from our ByteBuffer to the newly generated texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
        GlStateManager._bindTexture(0);

        //Free the memory from making the image
        STBImage.stbi_image_free(imageData);
    }

    /**
     * Binds this texture to the specified texture unit.
     * @param textureUnit The texture unit to bind to.
     */
    public void bind(int textureUnit) {
        GlStateManager._activeTexture(GL_TEXTURE0 + textureUnit);
        GlStateManager._bindTexture(handle);
    }

    public void close() {
        GlStateManager._deleteTexture(handle);
    }
}
