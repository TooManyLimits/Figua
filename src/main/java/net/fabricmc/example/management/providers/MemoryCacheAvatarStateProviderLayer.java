package net.fabricmc.example.management.providers;

import net.fabricmc.example.avatars.AvatarState;
import net.minecraft.entity.Entity;

import java.util.HashMap;

public class MemoryCacheAvatarStateProviderLayer extends AvatarStateProviderLayer {

    private HashMap<Entity, AvatarState> cache = new HashMap<>();

    @Override
    protected AvatarState attemptGetFor(Entity e) {
        return cache.get(e);
    }

    @Override
    protected void handleProvidedAvatar(Entity e, AvatarState state) {
        cache.put(e, state);
    }
}
