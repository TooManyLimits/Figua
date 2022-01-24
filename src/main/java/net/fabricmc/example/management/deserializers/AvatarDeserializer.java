package net.fabricmc.example.management.deserializers;

import net.fabricmc.example.avatars.Avatar;
import net.fabricmc.example.rendering.VAO;
import net.fabricmc.example.rendering.textures.FiguaTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

import java.util.UUID;

public class AvatarDeserializer implements NbtDeserializer<Avatar> {

    public static String VERSION = "0.1.0";

    public Avatar deserialize(NbtElement element) {
        if (element.getType() != NbtElement.COMPOUND_TYPE)
            throw new IllegalArgumentException("Illegal nbt type, must be Compound.");
        NbtCompound nbt = (NbtCompound) element;

        VAO vao = null;
        FiguaTexture tex = null;
        String luaSource = null;
        NbtCompound rootPart = null;

        NbtCompound modelElement = nbt.getCompound("model");
        if (modelElement != null) {
            vao = new VertexDataDeserializer().deserialize(modelElement);
            rootPart = modelElement.getCompound("root");
        }

        NbtElement textureElement = nbt.get("texture");
        if (textureElement != null)
            tex = new FiguaTextureDeserializer().deserialize(textureElement);

        NbtElement scriptElement = nbt.get("script");
        if (scriptElement != null)
            luaSource = new ScriptDeserializer().deserialize(scriptElement);

        UUID uuid = UUID.randomUUID(); //idk lol

        return new Avatar(uuid, tex, vao, rootPart, luaSource);
    }



    public boolean acceptsVersion(String version) {
        return version.equals(VERSION);
    }

}
