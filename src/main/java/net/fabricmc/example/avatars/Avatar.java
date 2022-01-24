package net.fabricmc.example.avatars;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.example.rendering.RenderUtils;
import net.fabricmc.example.rendering.VAO;
import net.fabricmc.example.rendering.textures.FiguaTexture;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

/**
 * An avatar does NOT store state! It is a collection of resources which can be
 * shared between all instances of this avatar, including the VAO and texture.
 *
 * Also contains some identifying information like a UUID for the avatar.
 */
public class Avatar {

    private UUID uuid;

    //The actual texture for this avatar.
    //TODO: Allow multiple textures, such as emissive
    private FiguaTexture texture;

    //The VAO holding the vertex data of this avatar.
    private VAO vao;

    //These are used for instantiating the avatar into an AvatarState.
    private NbtCompound modelNbt;
    private String luaSource;

    /**
     * Simple constructor, just assigns the variables as given.
     * Should not be used in normal circumstances, instead use an
     * NbtDeserializer<Avatar>.
     */
    public Avatar(UUID uuid, FiguaTexture figuaTexture, VAO vao, NbtCompound modelNbt, String luaSource) {
        this.uuid = uuid;
        this.texture = figuaTexture;
        this.vao = vao;
        this.modelNbt = modelNbt;
        this.luaSource = luaSource;
    }

    /**
     * Instantiates an AvatarState which uses this avatar.
     * @param owner The entity to which this state will be attached.
     * @return The new AvatarState.
     */
    public AvatarState instantiate(Entity owner) {
        return new AvatarState(this, modelNbt, luaSource, owner);
    }

    /**
     * The rendering phase controlled by the avatar.
     * Binds the regular Figua Texture, uploads the minecraft projection matrix, and draws the VAO.
     */
    public void render() {
        //Enable depth testing
        GlStateManager._enableDepthTest();
        //Bind figua texture
        texture.bind(0);
        //Upload Projection Matrix
        RenderUtils.defaultFiguaShader().setUniform("ProjMat", RenderUtils.getMCProjectionMatrix());
        //Make draw call
        vao.draw();
        //Disable depth test again
        GlStateManager._disableDepthTest();
    }

    public void close() {
        vao.close();
        texture.close();
    }

}
