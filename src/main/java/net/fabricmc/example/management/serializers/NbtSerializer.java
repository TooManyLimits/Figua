package net.fabricmc.example.management.serializers;

import net.minecraft.nbt.NbtElement;

/**
 * Something which accepts a T and returns an NbtElement.
 * @param <T>
 */
public interface NbtSerializer<T> {
    NbtElement serialize(T data);
}
