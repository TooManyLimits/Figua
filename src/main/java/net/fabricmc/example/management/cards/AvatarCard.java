package net.fabricmc.example.management.cards;

import net.fabricmc.example.avatars.Avatar;
import net.fabricmc.example.management.deserializers.FiguaTextureDeserializer;
import net.fabricmc.example.management.deserializers.ScriptDeserializer;
import net.fabricmc.example.management.deserializers.VertexDataDeserializer;
import net.fabricmc.example.rendering.VAO;
import net.fabricmc.example.rendering.textures.FiguaTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Upon request, provides an avatar. Requests to fill the avatar
 * if it was not already done.
 * @param <T> The type of data used to request the avatar. For
 *           backend or cache purposes it's a UUID, and for local
 *           avatars it's some way of referencing a file.
 */
public abstract class AvatarCard<T> {

    protected Avatar avatar;
    protected CardMetadata metadata;
    protected T data;

    /**
     * Creates a new card and begins to fill it asynchronously.
     * @param data
     */
    public AvatarCard(T data) {
        this.data = data;
        avatar = new Avatar();
        metadata = new CardMetadata();
        fillCard();
    }

    /**
     * Gets the avatar inside this card. May not be ready yet.
     */
    public Avatar getAvatar() {
        return avatar;
    }

    /**
     * Gets the metadata for this card. May not be ready yet.
     */
    public CardMetadata getMetadata() {
        return metadata;
    }

    /**
     * Asynchronously (or synchronously, if you want I guess...) fills
     * the information for this card.
     */
    protected abstract void fillCard();

    /**
     * Helper method for filling an avatar once you have the NBT for it
     * @param nbt The NBT of the avatar
     */
    protected void fillAvatarFromNbt(NbtCompound nbt) {
        VAO vao = null;
        FiguaTexture texture = null;
        NbtCompound rootPart = null;
        String luaSource = null;

        //Get the model NBT, if it exists, and convert to a VAO.
        NbtCompound modelElement = nbt.getCompound("model");
        if (modelElement != null) {
            vao = new VertexDataDeserializer().deserialize(modelElement);
            rootPart = modelElement.getCompound("root");
        }

        //Get the texture NBT if it exists, convert to FiguaTexture
        NbtElement textureElement = nbt.get("texture");
        if (textureElement != null) {
            texture = new FiguaTextureDeserializer().deserialize(textureElement);
        }

        //Get the script if it exists, convert to string
        NbtElement scriptElement = nbt.get("script");
        if (scriptElement != null) {
            luaSource = new ScriptDeserializer().deserialize(scriptElement);
        }

        //Finally, fill the avatar with the given info
        avatar.fill(vao, rootPart, texture, luaSource);
    }

}
