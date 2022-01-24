package net.fabricmc.example.management.deserializers;

import net.fabricmc.example.math.MathUtils;
import net.fabricmc.example.math.Vector2;
import net.fabricmc.example.math.Vector3;
import net.fabricmc.example.rendering.RenderUtils;
import net.fabricmc.example.rendering.VAO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Creates a VAO from a model NBT compound.
 */
public class VertexDataDeserializer implements NbtDeserializer<VAO> {

    public static String VERSION = "0.1.0";

    public VAO deserialize(NbtElement modelNbt) {

        if (modelNbt.getType() != NbtElement.COMPOUND_TYPE)
            throw new IllegalArgumentException("Illegal nbt type, must be Compound.");
        NbtCompound nbt = (NbtCompound) modelNbt;

        String version = nbt.getString("version");
        if (!version.equals(VERSION))
            throw new IllegalArgumentException("Invalid version. Requires " + VERSION + ", got " + version + ".");

        int numVertices = nbt.getInt("vertexCount");
        int numIndices = nbt.getInt("indexCount");
        int numCuboids = nbt.getInt("numCuboids");

        Vector2 texSize = new Vector2(nbt.getInt("texWidth"), nbt.getInt("texHeight"));

        ByteBuffer vertexData = BufferUtils.createByteBuffer(RenderUtils.defaultVertexLayout().getBytesPerVertex() * numVertices);
        IntBuffer indexData = BufferUtils.createIntBuffer(numIndices);

        byte[] inBytes = nbt.getByteArray("vertexData");
        ByteBuffer bytes = ByteBuffer.wrap(inBytes);

        for (int i = 0; i < numCuboids; i++)
            processCuboid(bytes, vertexData, indexData, texSize, i);

        vertexData.flip();
        indexData.flip();

        //System.out.println(vertexData.remaining() + " vertex bytes remaining");
        //System.out.println(indexData.remaining() + " index bytes remaining");
        return new VAO(vertexData, indexData, RenderUtils.defaultVertexLayout());
    }

    private static void processCuboid(ByteBuffer in, ByteBuffer vertexOut, IntBuffer indexOut, Vector2 texSize, int index) {
        int flags = in.getInt();
        Vector3 from = MathUtils.partToWorld(new Vector3(
                readFloatOrShort(in, flags, 0),
                readFloatOrShort(in, flags, 1),
                readFloatOrShort(in, flags, 2)));
        Vector3 to = MathUtils.partToWorld(new Vector3(
                readFloatOrShort(in, flags, 3),
                readFloatOrShort(in, flags, 4),
                readFloatOrShort(in, flags, 5)));

        //North
        int tex = in.get();
        if (tex != -1) {
            int rot = in.get();
            float u1 = readFloatOrShort(in, flags, 6);
            float v1 = readFloatOrShort(in, flags, 7);
            float u2 = readFloatOrShort(in, flags, 8);
            float v2 = readFloatOrShort(in, flags, 9);
            List<Vector2> uvs = rotateUV(u1, v1, u2, v2, rot);
            addFace(new Vector3(to.x(), to.y(), from.z()),
                    new Vector3(from.x(), to.y(), from.z()),
                    new Vector3(from.x(), from.y(), from.z()),
                    new Vector3(to.x(), from.y(), from.z()),
                    uvs, texSize, index, vertexOut, indexOut);
        }
        //East
        tex = in.get();
        if (tex != -1) {
            int rot = in.get();
            float u1 = readFloatOrShort(in, flags, 10);
            float v1 = readFloatOrShort(in, flags, 11);
            float u2 = readFloatOrShort(in, flags, 12);
            float v2 = readFloatOrShort(in, flags, 13);
            List<Vector2> uvs = rotateUV(u1, v1, u2, v2, rot);
            addFace(new Vector3(to.x(), to.y(), to.z()),
                    new Vector3(to.x(), to.y(), from.z()),
                    new Vector3(to.x(), from.y(), from.z()),
                    new Vector3(to.x(), from.y(), to.z()),
                    uvs, texSize, index, vertexOut, indexOut);
        }
        //South
        tex = in.get();
        if (tex != -1) {
            int rot = in.get();
            float u1 = readFloatOrShort(in, flags, 14);
            float v1 = readFloatOrShort(in, flags, 15);
            float u2 = readFloatOrShort(in, flags, 16);
            float v2 = readFloatOrShort(in, flags, 17);
            List<Vector2> uvs = rotateUV(u1, v1, u2, v2, rot);
            addFace(new Vector3(from.x(), to.y(), to.z()),
                    new Vector3(to.x(), to.y(), to.z()),
                    new Vector3(to.x(), from.y(), to.z()),
                    new Vector3(from.x(), from.y(), to.z()),
                    uvs, texSize, index, vertexOut, indexOut);
        }
        //West
        tex = in.get();
        if (tex != -1) {
            int rot = in.get();
            float u1 = readFloatOrShort(in, flags, 18);
            float v1 = readFloatOrShort(in, flags, 19);
            float u2 = readFloatOrShort(in, flags, 20);
            float v2 = readFloatOrShort(in, flags, 21);
            List<Vector2> uvs = rotateUV(u1, v1, u2, v2, rot);
            addFace(new Vector3(from.x(), to.y(), from.z()),
                    new Vector3(from.x(), to.y(), to.z()),
                    new Vector3(from.x(), from.y(), to.z()),
                    new Vector3(from.x(), from.y(), from.z()),
                    uvs, texSize, index, vertexOut, indexOut);
        }
        //Up
        tex = in.get();
        if (tex != -1) {
            int rot = in.get();
            float u1 = readFloatOrShort(in, flags, 22);
            float v1 = readFloatOrShort(in, flags, 23);
            float u2 = readFloatOrShort(in, flags, 24);
            float v2 = readFloatOrShort(in, flags, 25);
            List<Vector2> uvs = rotateUV(u1, v1, u2, v2, rot);
            addFace(new Vector3(from.x(), to.y(), from.z()),
                    new Vector3(to.x(), to.y(), from.z()),
                    new Vector3(to.x(), to.y(), to.z()),
                    new Vector3(from.x(), to.y(), to.z()),
                    uvs, texSize, index, vertexOut, indexOut);
        }
        //Down
        tex = in.get();
        if (tex != -1) {
            int rot = in.get();
            float u1 = readFloatOrShort(in, flags, 26);
            float v1 = readFloatOrShort(in, flags, 27);
            float u2 = readFloatOrShort(in, flags, 28);
            float v2 = readFloatOrShort(in, flags, 29);
            List<Vector2> uvs = rotateUV(u1, v1, u2, v2, rot);
            addFace(new Vector3(from.x(), from.y(), to.z()),
                    new Vector3(to.x(), from.y(), to.z()),
                    new Vector3(to.x(), from.y(), from.z()),
                    new Vector3(from.x(), from.y(), from.z()),
                    uvs, texSize, index, vertexOut, indexOut);
        }

    }

    /**
     * Top left, proceed clockwise.
     * For up and down faces, check blockbench for which is considered "top left"
     * @param p1 Top left
     * @param p2 Top right
     * @param p3 Bottom right
     * @param p4 Bottom left
     * @param uvs List of UV positions for each corner, in the same order
     * @param texSize The size of the texture
     * @param vertexData The buffer to write vertex data into
     * @param indexData The other buffer to write index data into
     */
    private static void addFace(Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p4,
                                List<Vector2> uvs, Vector2 texSize, int partIndex,
                                ByteBuffer vertexData, IntBuffer indexData) {
        int verticesAdded = vertexData.position() / RenderUtils.defaultVertexLayout().getBytesPerVertex();
        Vector3 normal = p1.subtract(p2).cross(p3.subtract(p2)).normalized();
        vertex(vertexData, p1, uvs.get(0).divide(texSize), normal, partIndex);
        vertex(vertexData, p2, uvs.get(1).divide(texSize), normal, partIndex);
        vertex(vertexData, p3, uvs.get(2).divide(texSize), normal, partIndex);
        vertex(vertexData, p4, uvs.get(3).divide(texSize), normal, partIndex);

        indexData.put(verticesAdded).put(verticesAdded + 2).put(verticesAdded + 1)
                        .put(verticesAdded).put(verticesAdded + 3).put(verticesAdded + 2);
    }

    private static void vertex(ByteBuffer vertexData, Vector3 pos, Vector2 uv, Vector3 normal, int index) {
        vertexData
                .putFloat((float) pos.x()).putFloat((float) pos.y()).putFloat((float) pos.z()) //Pos
                .put((byte) 0xFF).put((byte) 0xFF).put((byte) 0xFF).put((byte) 0xFF) //Color
                .putFloat((float) uv.x()).putFloat((float) uv.y()) //UV
                .putFloat((float) normal.x()).putFloat((float) normal.y()).putFloat((float) normal.z())
                .putShort((short) index);
    }

    private static List<Vector2> rotateUV(double u1, double v1, double u2, double v2, int rot) {
        List<Vector2> cornerUVs = new LinkedList<>();
        cornerUVs.add(new Vector2(u1, v1));
        cornerUVs.add(new Vector2(u2, v1));
        cornerUVs.add(new Vector2(u2, v2));
        cornerUVs.add(new Vector2(u1, v2));
        for (int i = 0; i < rot; i++)
            cornerUVs.add(0, cornerUVs.remove(3));
        return cornerUVs;
    }

    private static float readFloatOrShort(ByteBuffer in, int flags, int index) {
        if ((flags & (1 << index)) == 0) {
            return in.getShort();
        } else {
            return in.getFloat();
        }
    }

    private static void makeFace(Vector3 point1, Vector3 point2, Vector3 point3, Vector3 point4) {

    }

}
