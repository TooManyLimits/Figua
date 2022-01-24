package net.fabricmc.example.management.providers;

import net.fabricmc.example.avatars.AvatarState;
import net.minecraft.entity.Entity;

public abstract class AvatarStateProviderLayer {

    private AvatarStateProviderLayer next;

    public void setNext(AvatarStateProviderLayer nextLayer) {
        next = nextLayer;
    }

    /**
     * Attempts to get an AvatarState from this layer.
     * Returns null if this layer is unable to get the AvatarState.
     * Otherwise, returns the AvatarState.
     */
    protected abstract AvatarState attemptGetFor(Entity e);

    /**
     * Some method called on any avatar state returned from a lower layer.
     * Needs to handle null, since there might not be any avatar.
     * @param result The avatar which a lower layer has passed back up
     */
    protected void handleProvidedAvatar(Entity e, AvatarState result) {

    }

    /**
     * Gets an avatar state for this entity, going through all layers below recursively.
     * @param e The entity
     * @return An avatar state for this entity
     */
    public final AvatarState getAvatarStateFor(Entity e) {
        AvatarState result = attemptGetFor(e);
        if (result == null && next != null)
            result = next.getAvatarStateFor(e);
        handleProvidedAvatar(e, result);
        return result;
    }

}
