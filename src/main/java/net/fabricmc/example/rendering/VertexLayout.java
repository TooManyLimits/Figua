package net.fabricmc.example.rendering;

import com.mojang.blaze3d.platform.GlStateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL32.*;

/**
 * Represents a layout of vertex data.
 */
public class VertexLayout {

    private final List<Integer> data;
    private int bytesPerVertex;
    private int attributeCount;

    public VertexLayout() {
        data = new ArrayList<>();
        bytesPerVertex = 0;
        attributeCount = 0;
    }

    /**
     * Adds an additional attribute to this vertex layout.
     * It appends to the end of the current set of attributes, so the order
     * of calling attribute() does matter.
     * Example: I want to create a vec4 out of 4 floats.
     * attribute(GL_FLOAT, 4, false, false);
     * @param type A GL constant for the type of the vertex attribute.
     * @param count How many copies of that constant are used.
     * @param normalized Whether or not to normalize this attribute.
     * @param integer Whether or not this attribute should be passed as integer.
     * @return This instance, to allow chaining calls.
     */
    public VertexLayout attribute(int type, int count, boolean normalized, boolean integer) {
        data.add(count);
        data.add(type);
        data.add(normalized ? 1 : 0);
        data.add(integer ? 1 : 0);
        bytesPerVertex += count * typesToBytes.get(type);
        attributeCount++;
        return this;
    }

    /**
     * Sets up attrib pointers. Should be called only
     * once per VAO created with this layout.
     */
    public void setupAttribPointers() {
        int offset = 0;
        for (int i = 0; i < attributeCount*4; i+=4) {
            int count = data.get(i);
            int type = data.get(i + 1);
            if (data.get(i + 3) == 0)
                GlStateManager._vertexAttribPointer(i / 4, count, type, data.get(i+2) == 1, bytesPerVertex, offset);
            else
                GlStateManager._vertexAttribIPointer(i / 4, count, type, bytesPerVertex, offset);
            GlStateManager._enableVertexAttribArray(i / 4);
            offset += VertexLayout.typesToBytes.get(type) * count;
        }
    }

    /**
     * Returns the number of bytes per vertex of this layout.
     */
    public int getBytesPerVertex() {
        return bytesPerVertex;
    }

    /**
     * A static map that keeps track of the number of bytes in each GL type.
     */
    private static final HashMap<Integer, Integer> typesToBytes = new HashMap<>(){{
        put(GL_BYTE, Byte.BYTES);
        put(GL_UNSIGNED_BYTE, Byte.BYTES);
        put(GL_SHORT, Short.BYTES);
        put(GL_UNSIGNED_SHORT, Short.BYTES);
        put(GL_FLOAT, Float.BYTES);
        put(GL_INT, Integer.BYTES);
        put(GL_UNSIGNED_INT, Integer.BYTES);
    }};

}
