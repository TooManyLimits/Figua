package net.fabricmc.example.management.serializers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.nio.file.Path;

/**
 * Accepts a path to a local avatar folder,
 * and returns an NbtCompound containing all info
 * of the avatar.
 * This compound can be treated the same as any downloaded
 * from the backend.
 */
public class LocalAvatarSerializer implements NbtSerializer<Path> {

    private final BBModelSerializer modelSerializer;
    private final PngImageSerializer textureSerializer;
    private final ScriptSerializer scriptSerializer;

    public LocalAvatarSerializer() {
        this(new BBModelSerializer(), new PngImageSerializer(), new ScriptSerializer());
    }

    public LocalAvatarSerializer(BBModelSerializer modelSerializer, PngImageSerializer textureSerializer, ScriptSerializer scriptSerializer) {
        this.modelSerializer = modelSerializer;
        this.textureSerializer = textureSerializer;
        this.scriptSerializer = scriptSerializer;
    }

    //TODO: get card metadata, don't hardcode names
    public NbtCompound serialize(Path p) {
        Path modelPath = p.resolve("model.bbmodel");
        Path texturePath = p.resolve("texture.png");
        Path scriptPath = p.resolve("script.lua");

        NbtCompound avatarCompound = new NbtCompound();

        NbtElement modelElement = modelSerializer.serialize(modelPath);
        if (modelElement != null)
            avatarCompound.put("model", modelElement);

        NbtElement textureElement = textureSerializer.serialize(texturePath);
        if (textureElement != null)
            avatarCompound.put("texture", textureElement);

        NbtElement scriptElement = scriptSerializer.serialize(scriptPath);
        if (scriptElement != null)
            avatarCompound.put("script", scriptElement);

        return avatarCompound;
    }

}
