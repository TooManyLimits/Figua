package net.fabricmc.example.avatars;

import net.fabricmc.example.lua.LuaManager;
import net.fabricmc.example.management.deserializers.ModelPartDeserializer;
import net.fabricmc.example.math.MathUtils;
import net.fabricmc.example.math.Matrix4;
import net.fabricmc.example.rendering.RenderUtils;
import net.fabricmc.example.rendering.textures.TransformTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.terasology.jnlua.LuaState;

public class AvatarState {

    private final Entity user;
    private final Avatar avatar;
    private final FiguaModelPart rootModelPart;
    private final TransformTexture transforms;
    private LuaState luaState;

    public AvatarState(Avatar avatar, NbtCompound modelNbt, String luaSource, Entity entity) {
        this.user = entity;
        this.avatar = avatar;
        rootModelPart = new ModelPartDeserializer().deserialize(modelNbt);
        transforms = new TransformTexture(rootModelPart.getLastChildTexIndex() + 1);

        if (luaSource != null) {
            luaState = LuaManager.createLuaState();
            rootModelPart.createAsGlobal(luaState, "model");
            LuaManager.setupLog(luaState);
            LuaManager.runSource(luaState, luaSource);
        }
    }

    public void luaTick() {
        //Push events
        luaState.getGlobal("events");
        //Push events.tick
        luaState.getField(-1, "tick");
        //Push events.tick.invoke
        luaState.getField(-1, "invoke");
        //Push events.tick again
        luaState.pushValue(-2);
        //Call invoke, pop events.tick.invoke and events.tick
        luaState.call(1, 0);
        //Pop events.tick and events
        luaState.pop(2);
    }

    public void luaRender(float delta) {
        //System.out.println(luaState.getTotalMemory() - luaState.getFreeMemory());
        luaState.getGlobal("events");
        luaState.getField(-1, "render");
        luaState.getField(-1, "invoke");
        luaState.pushValue(-2);
        luaState.pushNumber(delta);
        luaState.call(2, 0);
        luaState.pop(2);
    }

    private static final Matrix4 SCALE_MATRIX = Matrix4.scale(1.0/16, 1.0/16, 1.0/16);

    /**
     * Renders this AvatarState with the given tick delta.
     * @param delta The proportion of a tick that has passed at the time this frame was rendered.
     */
    public void render(float delta) {
        luaRender(delta);
        //Set up all model parts' transforms prior to rendering
        rootModelPart.recursiveSetupPreRender(SCALE_MATRIX, false, transforms, null);

        //Use the shaders
        RenderUtils.defaultFiguaShader().use();
        //Set up the modelview matrix for the entity
        Matrix4 modelView = MathUtils.entityToWorldMatrix(user).multiply(MathUtils.worldToViewMatrix());
        RenderUtils.defaultFiguaShader().setUniform("ModelViewMat", modelView);

        //Bind our transform texture to unit 1
        transforms.bind(1);
        //Render the avatar itself
        avatar.render();
    }

    public void close() {
        transforms.close();
        luaState.close();
    }

}
