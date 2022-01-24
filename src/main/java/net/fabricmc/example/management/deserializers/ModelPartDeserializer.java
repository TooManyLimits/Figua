package net.fabricmc.example.management.deserializers;

import net.fabricmc.example.avatars.FiguaModelPart;
import net.fabricmc.example.math.Vector3;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;

public class ModelPartDeserializer implements NbtDeserializer<FiguaModelPart> {

    public FiguaModelPart deserialize(NbtElement rootPart) {
        if (rootPart.getType() != NbtElement.COMPOUND_TYPE)
            throw new IllegalArgumentException("Illegal nbt type, must be Compound.");
        FiguaModelPart result = deserializeHelper((NbtCompound) rootPart);
        FiguaModelPart.resetBuildTree();

        return result;
    }

    /**
     * Recursively deserializes a part, creating it as well as its children.
     * @param partNbt An nbtCompound representing the part.
     * @return The part, with all of its children attached.
     */
    private FiguaModelPart deserializeHelper(NbtCompound partNbt) {
        Vector3 pivot = Vector3.ZERO;
        Vector3 rot = Vector3.ZERO;
        boolean hasVertexData = false;
        boolean visibility = true;

        if (partNbt.contains("pivot"))
            pivot = nbtToVector3(partNbt.getList("pivot", NbtElement.FLOAT_TYPE));
        if (partNbt.contains("rotation"))
            rot = nbtToVector3(partNbt.getList("rotation", NbtElement.FLOAT_TYPE));
        if (partNbt.contains("visibility"))
            visibility = partNbt.getBoolean("visibility");
        boolean hasChildren = partNbt.contains("children");
        if (!hasChildren)
            hasVertexData = true;
        String name = partNbt.getString("name");

        FiguaModelPart part = new FiguaModelPart(name, pivot, rot, hasVertexData, visibility);
        if (hasChildren) {
            NbtList children = partNbt.getList("children", NbtElement.COMPOUND_TYPE);
            for (NbtElement child : children) {
                part.addChild(deserializeHelper((NbtCompound) child));
            }
        }

        return part;
    }

    private static Vector3 nbtToVector3(NbtList list) {
        float x = ((NbtFloat) list.get(0)).floatValue();
        float y = ((NbtFloat) list.get(1)).floatValue();
        float z = ((NbtFloat) list.get(2)).floatValue();
        return new Vector3(x, y, z);
    }

}
