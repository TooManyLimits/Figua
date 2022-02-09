package net.fabricmc.example.mixin;

import net.fabricmc.example.FiguaMod;
import net.fabricmc.example.avatars.AvatarState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void testDrawing(Entity e, float f, float g, MatrixStack m, VertexConsumerProvider vcp, int l, CallbackInfo ci) {
        List<AvatarState> states = FiguaMod.AVATAR_STATE_MANAGER.getStatesFor(e);
        for (AvatarState state : states)
            state.render(g);
    }
}