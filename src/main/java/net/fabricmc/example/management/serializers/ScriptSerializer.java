package net.fabricmc.example.management.serializers;

import net.fabricmc.example.FiguaMod;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Kinda silly to have as a class, just here for
 * consistency with the other serializers
 */
public class ScriptSerializer implements NbtSerializer<Path> {

    public NbtElement serialize(Path path) {
        try {
            if (Files.exists(path)) {
                String luaSource = Files.readString(path);
                return NbtString.of(minify(luaSource));
            }
        } catch (IOException e) {
            FiguaMod.LOGGER.error(e);
        }
        return null;
    }

    //TODO: implement minifier
    private static String minify(String source) {
        return source;
    }

}
