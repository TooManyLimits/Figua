package net.fabricmc.example.management.deserializers;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

/**
 * Really basic class... just here for consistency with other parts
 * of avatar loading.
 */
public class ScriptDeserializer implements NbtDeserializer<String> {

    public String deserialize(NbtElement element) {
        if (element.getType() != NbtElement.STRING_TYPE)
            throw new IllegalArgumentException("Illegal nbt type, must be String.");
        NbtString nbtString = (NbtString) element;
        return nbtString.asString();
    }
}
