package net.fabricmc.example.management;

import net.fabricmc.example.avatars.Avatar;
import net.fabricmc.example.avatars.AvatarState;
import net.fabricmc.example.management.cards.CardDeck;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AvatarStateManager {

    private final Map<Entity, CardDeck> cardDeckMap = new HashMap<>();
    private final Map<Entity, List<AvatarState>> cachedAvatarStates = new HashMap<>();


    private static final List<AvatarState> NO_AVATARS = new ArrayList<>(0);

    /**
     * Here for testing. Probably should have a better way of doing this
     * @param e The entity to put the deck on.
     * @param deck The deck to assign to this entity.
     */
    public void addDeckTo(Entity e, CardDeck deck) {
        cardDeckMap.put(e, deck);
    }

    /**
     * Entities can have multiple AvatarStates on them at once, potentially.
     * Caches the result for future calls.
     * @param e The entity to get the AvatarStates for.
     * @return A list of all the AvatarStates this entity has.
     */
    public List<AvatarState> getStatesFor(Entity e) {
        if (!cachedAvatarStates.containsKey(e)) {
            CardDeck deck = cardDeckMap.get(e);
            if (deck == null)
                cachedAvatarStates.put(e, NO_AVATARS);
            else {
                List<Avatar> avatars = deck.getAvatars();
                List<AvatarState> result = new ArrayList<>(avatars.size());
                AtomicBoolean failedLoad = new AtomicBoolean(false);
                avatars.forEach(avatar -> {
                    if (avatar.isReady())
                        result.add(avatar.getStateFor(e));
                    else
                        failedLoad.set(true);
                });
                if (!failedLoad.get())
                    //Don't cache the result if not all the avatars were loaded yet.
                    cachedAvatarStates.put(e, result);
                else
                    return result;
            }
        }
        return cachedAvatarStates.get(e);
    }

    /**
     * Get AvatarStates for a player, specifically.
     * Getting the states for a player requires a different process than for normal entities.
     * @param player The player to get the AvatarStates for.
     * @return A list of all the AvatarStates for this player.
     */
    public List<AvatarState> getStatesFor(PlayerEntity player) {
        //TODO: Actually make this
        return getStatesFor((Entity) player);
    }


}
