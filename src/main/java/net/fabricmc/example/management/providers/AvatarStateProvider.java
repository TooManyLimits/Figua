package net.fabricmc.example.management.providers;

import net.fabricmc.example.avatars.AvatarState;
import net.minecraft.entity.Entity;

/**
 * This whole package is not great imo. Thinking about nuking it and trying again with different strategy
 */
public class AvatarStateProvider {

    private AvatarStateProviderLayer firstLayer;

    private AvatarStateProvider(AvatarStateProviderLayer layer1) {
        firstLayer = layer1;
    }

    public AvatarState getAvatarFor(Entity e) {
        //Recursively calls on lower layers
        return firstLayer.getAvatarStateFor(e);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private AvatarStateProviderLayer firstLayer;
        private AvatarStateProviderLayer lastLayer;

        private Builder() {}

        public Builder attach(AvatarStateProviderLayer nextLayer) {
            if (lastLayer != null)
                lastLayer.setNext(nextLayer);
            else
                firstLayer = nextLayer;
            lastLayer = nextLayer;
            return this;
        }

        public AvatarStateProvider build() {
            return new AvatarStateProvider(firstLayer);
        }

    }

}
