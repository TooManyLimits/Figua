package net.fabricmc.example.management.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.example.FiguaMod;
import net.fabricmc.example.math.Vector3;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts a .bbmodel file into an NBT compound.
 * Uses a format of my own creation which (hopefully) will reduce file size.
 */
public class BBModelSerializer implements NbtSerializer<Path> {

    public static final String VERSION = "0.1.0";

    public NbtCompound serialize(Path path) {
        try {
            if (Files.exists(path)) {
                String jsonString = Files.readString(path);
                return parse(jsonString);
            }
        } catch (IOException e) {
            FiguaMod.LOGGER.error(e);
        }
        return null;
    }

    /**
     * Converts a json string to a model data compound.
     * The format is:
     *
     * version: [String] // The version which was used to encode this NbtCompound.
     * vertexData: [Byte Array] // See processCuboid method for format
     * texWidth: [Int]
     * texHeight: [Int]
     * root: [Model Part] // The root Model Part, which contains all other Model Parts
     * // A Model Part is an NbtCompound which contains:
     * - name: [String]
     * - pivot: [Float List]
     * - rotation: [Float List]
     * - visibility: [Boolean]
     * - children: [NbtList of Model Part]
     * numCuboids: [Int]
     * vertexCount: [Int]
     * indexCount: [Int]
     *
     * @param json The json of a .bbmodel file, as a string.
     * @return An NbtCompound representing the contents of this bbmodel file.
     */
    public NbtCompound parse(String json) {

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        JsonArray elementArray = jsonObject.getAsJsonArray("elements");
        Map<String, JsonObject> elementMap = collectElementsByUUID(elementArray);

        JsonObject resolution = jsonObject.getAsJsonObject("resolution");
        int w = resolution.get("width").getAsInt();
        int h = resolution.get("height").getAsInt();

        JsonArray outliner = jsonObject.getAsJsonArray("outliner");
        NbtList modelPartList = new NbtList();
        ByteArrayOutputStream vertexData = new ByteArrayOutputStream();
        int[] counts = new int[3];
        processParts(elementMap, outliner, modelPartList, vertexData, counts);
        //System.out.println(modelPartList);

        NbtCompound rootPart = new NbtCompound();
        rootPart.putString("name", "root");
        rootPart.put("children", modelPartList);

        NbtCompound modelCompound = new NbtCompound();
        modelCompound.putString("version", VERSION);
        modelCompound.putInt("vertexCount", counts[0]);
        modelCompound.putInt("indexCount", counts[1]);
        modelCompound.putInt("numCuboids", counts[2]);
        modelCompound.putInt("texWidth", w);
        modelCompound.putInt("texHeight", h);


        byte[] bytes = vertexData.toByteArray();
//        try {
//            FileOutputStream fos = new FileOutputStream("C:\\Users\\tball\\Desktop\\Minecraft Modding\\test.zip");
//            ZipOutputStream zos = new ZipOutputStream(fos);
//            zos.putNextEntry(new ZipEntry("Hi"));
//            zos.write(bytes);
//            zos.closeEntry();
//            zos.finish();
//            zos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        modelCompound.put("vertexData", new NbtByteArray(bytes));
        modelCompound.put("root", rootPart);
        return modelCompound;
    }

    /**
     * Processes all json elements in the given json array,
     * converts them to Model Part NbtCompounds, and puts
     * them in the provided list.
     * @param elementMap Map of the "elements" in the bbmodel file, the cuboids and meshes. Keys are uuids.
     * @param parts Array of parts to process, can contain either Json objects or uuid strings.
     * @param partList Nbt list to put the processed parts into.
     * @param vertexData ByteArrayOutputStream to put vertex data into.
     * @param counts Small 2-element array to keep track of vertexCount and indexCount.
     */
    public static void processParts(Map<String, JsonObject> elementMap, JsonArray parts, NbtList partList, ByteArrayOutputStream vertexData, int[] counts) {
        for (JsonElement jsonPart : parts) {
            NbtCompound part = new NbtCompound();
            boolean isGroup = jsonPart.isJsonObject();
            JsonObject obj;
            if (isGroup)
                obj = jsonPart.getAsJsonObject();
            else
                obj = elementMap.get(jsonPart.getAsString());
            part.putString("name", obj.get("name").getAsString());
            if (obj.has("origin"))
                part.put("pivot", vector3ToNbt(vector3FromJArray(obj.getAsJsonArray("origin"))));
            if (obj.has("rotation"))
                part.put("rotation", vector3ToNbt(vector3FromJArray(obj.getAsJsonArray("rotation"))));
            if (obj.has("visibility"))
                part.putBoolean("visibility", obj.get("visibility").getAsBoolean());
            if (obj.has("children")) {
                NbtList children = new NbtList();
                processParts(elementMap, obj.getAsJsonArray("children"), children, vertexData, counts);
                part.put("children", children);
            }
            if (!isGroup) {
                processCuboid(obj, vertexData, counts);
            }
            partList.add(part);
        }
    }

    /**
     * The format of cuboid storage is this:
     * - An int, of which the smallest 30 bits are used as flags for the specified "Numbers".
     * - A 0 means a short, and a 1 means a float.
     * - First 6 numbers are flags for "from" and "to".
     * - Remaining groups of 4 numbers are flags for the 6 faces.
     * - Some might be skipped in the event of empty faces.
     * 3 Numbers: "from"
     * 3 Numbers: "to"
     * The 6 sides, in order N, E, S, W, U, D
     * {
     *     1 byte, texture id. -1 if null.
     *     IF the texture id is not null:
     *     1 byte, rot. 0, 1, 2, or 3 depending on blockbench texture rotation.
     *     2 Numbers top-left UV
     *     2 Numbers bottom-right UV
     * }
     *
     * These numbers all end up in the List passed in.
     * @param cuboid The jsonObject containing the cuboid
     * @param bytes The arraylist for the result to be read into
     */
    private static void processCuboid(JsonObject cuboid, ByteArrayOutputStream bytes, int[] counts) {
        counts[2]++; //Num cuboids
        int flags = 0;

        double inflate = 0;
        if (cuboid.has("inflate"))
            inflate = cuboid.get("inflate").getAsDouble();

        Vector3 from = vector3FromJArray(cuboid.getAsJsonArray("from"));
        from = from.subtract(Vector3.ONE.scale(inflate));
        if (from.x() != Math.rint(from.x()))
            flags |= 1;
        if (from.y() != Math.rint(from.y()))
            flags |= 2;
        if (from.z() != Math.rint(from.z()))
            flags |= 4;

        Vector3 to = vector3FromJArray(cuboid.getAsJsonArray("to"));
        to = to.add(Vector3.ONE.scale(inflate));
        if (to.x() != Math.rint(to.x()))
            flags |= 8;
        if (to.y() != Math.rint(to.y()))
            flags |= 16;
        if (to.z() != Math.rint(to.z()))
            flags |= 32;

        JsonObject faces = cuboid.getAsJsonObject("faces");
        byte[] texes = new byte[6];
        byte[] rots = new byte[6];
        double[] uvs = new double[24];

        flags = processFace(0, faces.getAsJsonObject("north"), texes, rots, uvs, flags);
        flags = processFace(1, faces.getAsJsonObject("east"), texes, rots, uvs, flags);
        flags = processFace(2, faces.getAsJsonObject("south"), texes, rots, uvs, flags);
        flags = processFace(3, faces.getAsJsonObject("west"), texes, rots, uvs, flags);
        flags = processFace(4, faces.getAsJsonObject("up"), texes, rots, uvs, flags);
        flags = processFace(5, faces.getAsJsonObject("down"), texes, rots, uvs, flags);

        bytes.write((byte) (flags >> 24));
        bytes.write((byte) (flags >> 16));
        bytes.write((byte) (flags >> 8));
        bytes.write((byte) (flags));

        putFloatOrShort(bytes, flags, 0, from.x());
        putFloatOrShort(bytes, flags, 1, from.y());
        putFloatOrShort(bytes, flags, 2, from.z());
        putFloatOrShort(bytes, flags, 3, to.x());
        putFloatOrShort(bytes, flags, 4, to.y());
        putFloatOrShort(bytes, flags, 5, to.z());

        for (int i = 0; i < 6; i++) {
            bytes.write(texes[i]);
            if (texes[i] != -1) {
                bytes.write(rots[i]);
                counts[0] += 4; //vertexCount
                counts[1] += 6; //indexCount
                for (int j = 0; j < 4; j++) {
                    int index = i * 4 + j;
                    putFloatOrShort(bytes, flags, index + 6, uvs[index]);
                }
            }
        }
    }

    /**
     * Writes a value to the byte array, as either a float or short.
     * Which it is depends on the int flags, as well as the index passed in.
     * These flags are calculated based on whether val is a decimal or an integer.
     * @param bytes The location to write the number to
     * @param flags The flags to use
     * @param index The index of the flag to use
     * @param val The value to write
     */
    private static void putFloatOrShort(ByteArrayOutputStream bytes, int flags, int index, double val) {
        if ((flags & (1 << index)) == 0) {
            short num = (short) val;
            bytes.write((byte) (num >> 8));
            bytes.write((byte) (num));
        } else {
            int num = Float.floatToIntBits((float) val);
            bytes.write((byte) (num >> 24));
            bytes.write((byte) (num >> 16));
            bytes.write((byte) (num >> 8));
            bytes.write((byte) (num));
        }
    }

    /**
     * Processes a json face from the .bbmodel.
     * @param index The index of the face we're parsing
     * @param face The JsonObject containing the face data
     * @param texes An array which we write our output to, which texture this face uses
     * @param rots An array which we output to, the rotation of the texture on this face
     * @param uvs An array we output to, contains the U and V coordinates of this face
     * @param flags Flags which we modify then return, based on whether the UV coords are whole numbers.
     * @return flags, modified as above.
     */
    private static int processFace(int index, JsonObject face, byte[] texes, byte[] rots, double[] uvs, int flags) {
        JsonElement tex = face.get("texture");
        if (tex != null && tex.isJsonNull()) {
            texes[index] = -1;
        } else {
            if (face.has("rotation"))
                rots[index] = (byte) Math.round(face.get("rotation").getAsDouble()/90);
            if (tex != null)
                texes[index] = tex.getAsByte();
            else
                texes[index] = 0;
            JsonArray uv = face.getAsJsonArray("uv");

            for (int i = 0; i < 4; i++) {
                uvs[4 * index + i] = uv.get(i).getAsDouble();
                if (uvs[4 * index + i] != Math.rint(uvs[4 * index + i]))
                    flags |= (1 << (4 * index + i + 6));
            }
        }
        return flags;
    }

    /**
     * Creates an easy map from UUID -> JsonObject so we can
     * easily find elements referenced in the outliner.
     * @param elementArray The array of elements.
     * @return A map from UUIDs to Elements.
     */
    private static Map<String, JsonObject> collectElementsByUUID(JsonArray elementArray) {
        Map<String, JsonObject> result = new HashMap<>();
        for (JsonElement element : elementArray) {
            if (!element.isJsonObject())
                continue;
            JsonObject obj = element.getAsJsonObject();
            if (!obj.has("uuid"))
                continue;
            result.put(obj.get("uuid").getAsString(), obj);
        }
        return result;
    }

    private static Vector3 vector3FromJArray(JsonArray array) {
        return new Vector3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }

    private static NbtList vector3ToNbt(Vector3 vec) {
        NbtList result = new NbtList();
        result.add(NbtFloat.of((float) vec.x()));
        result.add(NbtFloat.of((float) vec.y()));
        result.add(NbtFloat.of((float) vec.z()));
        return result;
    }
}
