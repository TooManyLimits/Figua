package net.fabricmc.example.management.deserializers;

import net.minecraft.nbt.NbtElement;

/**
 * The opposite of an NbtSerializer.
 * Accepts an NbtElement and returns a T.
 * @param <T>
 */
public interface NbtDeserializer<T> {
    T deserialize(NbtElement nbt);
}
