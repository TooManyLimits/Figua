package net.fabricmc.example.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.BufferRenderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL32.*;

/**
 * A Vertex Array Object. Also contains references to a VBO and EBO.
 */
public class VAO {

    private final int vaoHandle;
    private final int vboHandle;
    private final int eboHandle;
    private final int numIndices;

    public VAO(ByteBuffer vertexData, IntBuffer indexData, VertexLayout layout) {
        BufferRenderer.unbindAll();
        numIndices = indexData.remaining();

        vaoHandle = GlStateManager._glGenVertexArrays();
        GlStateManager._glBindVertexArray(vaoHandle);

        vboHandle = GlStateManager._glGenBuffers();
        GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, vboHandle);
        GlStateManager._glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

        eboHandle = GlStateManager._glGenBuffers();
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboHandle);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW); //GlStateManager doesn't like non-ByteBuffers

        layout.setupAttribPointers();

        GlStateManager._glBindVertexArray(0);
    }

    /**
     * Deletes this VAO as well as its VBO and EBO.
     * Call when you don't need them anymore to free up VRAM.
     */
    public void close() {
        GlStateManager._glDeleteBuffers(vboHandle);
        GlStateManager._glDeleteBuffers(eboHandle);
        GlStateManager._glDeleteVertexArrays(vaoHandle);
    }

    /**
     * Binds this VAO.
     */
    public void bind() {
        GlStateManager._glBindVertexArray(vaoHandle);
    }

    /**
     * Binds and draws this VAO to the current framebuffer.
     */
    public void draw() {
        int lastVAO = GlStateManager._getInteger(GL_VERTEX_ARRAY_BINDING);
        BufferRenderer.unbindAll();
        bind();
        GlStateManager._drawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);
        GlStateManager._glBindVertexArray(lastVAO);
    }
}
