package net.fabricmc.example.gui.cards;

import net.fabricmc.example.management.cards.AvatarCard;
import net.fabricmc.example.rendering.VAO;

/**
 * A wrapper for a card, which is able to render the card within
 */
public class CardRenderer {

    /**
     * The card which this renderer wraps and draws.
     */
    private final AvatarCard backingCard;
    private VAO vao;

    public CardRenderer(AvatarCard card) {
        backingCard = card;
    }




}
