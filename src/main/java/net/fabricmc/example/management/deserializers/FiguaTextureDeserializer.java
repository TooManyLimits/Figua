package net.fabricmc.example.management.deserializers;

import net.fabricmc.example.rendering.textures.FiguaTexture;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtElement;

import java.io.ByteArrayInputStream;

public class FiguaTextureDeserializer implements NbtDeserializer<FiguaTexture> {

    public FiguaTexture deserialize(NbtElement nbt) {
        if (nbt.getType() != NbtElement.BYTE_ARRAY_TYPE)
            throw new IllegalArgumentException("Illegal nbt type, must be ByteArray.");
        NbtByteArray bytes = (NbtByteArray) nbt;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.getByteArray());
        return new FiguaTexture(inputStream);
    }

}
