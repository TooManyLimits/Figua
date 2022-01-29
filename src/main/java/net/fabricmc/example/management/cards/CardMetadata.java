package net.fabricmc.example.management.cards;

import net.fabricmc.example.FiguaMod;
import net.fabricmc.example.management.deserializers.FiguaTextureDeserializer;
import net.fabricmc.example.management.serializers.PngImageSerializer;
import net.fabricmc.example.rendering.textures.FiguaTexture;

import java.nio.file.Path;

/**
 * Contains information about a card which is needed to render the card.
 */
public class CardMetadata {

    private CardBack cardBack;
    private CardBackground cardBackground;
    private String cardLua;

    private boolean isReady;
    public void complete() {
        isReady = true;
    }
    public boolean isReady() {
        return isReady;
    }


    public void fill(CardBack cardBack, CardBackground cardBackground, String cardLua) {
        this.cardBack = cardBack;
        this.cardBackground = cardBackground;
        this.cardLua = cardLua;
    }

    //Overloads for fill
    public void fill(CardBack cardBack, CardBackground cardBackground) {
        fill(cardBack, cardBackground, null);
    }
    public void fill(String cardBack, String cardBackground, String cardLua) {
        fill(CardBack.valueOf(cardBack), CardBackground.valueOf(cardBackground), cardLua);
    }
    public void fill(String cardBack, String cardBackground) {
        fill(CardBack.valueOf(cardBack), CardBackground.valueOf(cardBackground), null);
    }

    public CardBack getCardBack() {
        if (!isReady)
            return CardBack.TEST;
        return cardBack;
    }

    public CardBackground getCardBackground() {
        if (!isReady)
            return CardBackground.TEST;
        return cardBackground;
    }

    /**
     * Enum holding all the card backs.
     */
    public enum CardBack {
        TEST(FiguaMod.getAssetPath().resolve("cards/backs/test.png"));

        private final FiguaTexture texture;
        CardBack(Path imagePath) {
            texture = new FiguaTextureDeserializer().deserialize(new PngImageSerializer().serialize(imagePath));
        }
        public FiguaTexture getTexture() {
            return texture;
        }
    }

    /**
     * Enum holding all the card backgrounds.
     */
    public enum CardBackground {
        TEST(FiguaMod.getAssetPath().resolve("cards/backgrounds/test.png"));

        private final FiguaTexture texture;
        CardBackground(Path imagePath) {
            texture = new FiguaTextureDeserializer().deserialize(new PngImageSerializer().serialize(imagePath));
        }
        public FiguaTexture getTexture() {
            return texture;
        }
    }
}
