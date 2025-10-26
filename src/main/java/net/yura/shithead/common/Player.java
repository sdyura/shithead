package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Shithead card game.
 */
public class Player {

    private String name;

    /**
     * these are the cards the player is physically holding right now
     */
    private final List<Card> hand = new ArrayList<>();

    private final List<Card> upcards = new ArrayList<>();
    private final List<Card> downcards = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public int getNoCards() {
        return hand.size() + upcards.size() + downcards.size();
    }

    public Card findLowestCard() {
        int bestRank = Integer.MAX_VALUE;
        Card lowest = null;
        for (Card c : getHand()) {
            int rank = c.getRank().toInt();
            if (rank >= 3 && rank < bestRank) {
                bestRank = rank;
                lowest = c;
            }
        }
        return lowest;
    }
}
