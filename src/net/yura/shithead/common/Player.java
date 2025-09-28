package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Hand;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Shithead card game.
 */
public class Player {

    /**
     * these are the cards the player is physically holding right now
     */
    private final List<Card> hand = new ArrayList<>();

    private final List<Card> upcards = new ArrayList<>();
    private final List<Card> downcards = new ArrayList<>();

    /**
     * Gets the cards currently in the players hand.
     */
    public List<Card> getHand() {
        return hand;
    }

    /**
     * Upcards that are visible on the table.
     */
    public List<Card> getUpcards() {
        return upcards;
    }

    /**
     * Downcards that are face down on the table.
     */
    public List<Card> getDowncards() {
        return downcards;
    }
}
