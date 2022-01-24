package net.fabricmc.example.management.serializers;

import net.minecraft.nbt.NbtByteArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts a .png file into an NBT byte array.
 */
public class PngImageSerializer implements NbtSerializer<Path> {

    public NbtByteArray serialize(Path path) {
        try {
            if (Files.exists(path))
                return new NbtByteArray(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
