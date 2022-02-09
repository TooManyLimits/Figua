package net.fabricmc.example.management.cards;

import net.fabricmc.example.avatars.Avatar;

import java.util.ArrayList;
import java.util.List;

public class CardDeck {

    private static final int ROWS = 3;
    private static final int COLS = 4;

    //The cache can be invalidated by simply setting cachedTopAvatars to null.
    private List<Avatar> cachedTopAvatars;

    /**
     * Empty slots in this array are null
     */
    private final AvatarCard[][] cards = new AvatarCard[ROWS][COLS];

    /**
     * Just creates a basic CardDeck with one card only, in the 1st row 1st column.
     * @param card The card to put in this slot.
     */
    public CardDeck(AvatarCard card) {
        cards[0][0] = card;
    }

    /**
     * Gets all the avatars for this user, up to a max of COLS.
     * Takes the topmost card in each column, and gets its avatar.
     * Caches the result for next time this is called.
     * @return A List containing the topmost Avatars in the deck.
     */
    public List<Avatar> getAvatars() {
        if (cachedTopAvatars == null) {
            cachedTopAvatars = new ArrayList<>(COLS);
            for (int x = 0; x < COLS; x++) {
                int y = 0;
                while (y < ROWS && cards[y][x] == null)
                    y++;
                if (y != ROWS)
                    cachedTopAvatars.add(cards[y][x].getAvatar());
            }
        }
        return cachedTopAvatars;
    }


}
