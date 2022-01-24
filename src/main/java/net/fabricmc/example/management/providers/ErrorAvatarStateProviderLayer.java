package net.fabricmc.example.management.providers;

import net.fabricmc.example.FiguaMod;
import net.fabricmc.example.avatars.Avatar;
import net.fabricmc.example.avatars.AvatarState;
import net.fabricmc.example.management.deserializers.AvatarDeserializer;
import net.fabricmc.example.management.serializers.LocalAvatarSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Provides an AvatarState of a spinning red mark 3 blocks above the ground.
 */
public class ErrorAvatarStateProviderLayer extends AvatarStateProviderLayer {

    private static Avatar errorAvatar;

    @Override
    protected AvatarState attemptGetFor(Entity e) {
        if (errorAvatar == null) {
            Path errorAvatarPath = FiguaMod.getAssetPath().resolve("avatars/error");
            NbtCompound avatarNbt = new LocalAvatarSerializer().serialize(errorAvatarPath);
            errorAvatar = new AvatarDeserializer().deserialize(avatarNbt);
        }
//        if (!(e instanceof PlayerEntity))
//            return null;
        return errorAvatar.instantiate(e);
    }
}
