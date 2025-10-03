package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.CardDeckEmptyException;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic implementation of the Shithead card game rules.
 * <p>
 * This class manages the deck, waste pile and players while exposing
 * helper methods to play cards and progress the game. It aims to mirror
 * the description of the game found on its Wikipedia page.
 */
public class ShitheadGame {

    private final List<Player> players = new ArrayList<>();
    private final Deck deck = new Deck(1);
    private final List<Card> wastePile = new ArrayList<>();

    private int currentPlayer;

    /**
     * Creates a new game with the given number of players.
     *
     * @param playerCount number of players taking part
     */
    public ShitheadGame(int playerCount) {
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player());
        }
    }

    /**
     * Deals three downcards, three upcards and three hand cards to each player.
     */
    public void deal() {
        deck.shuffle();
        try {
            for (int i = 0; i < 3; i++) {
                for (Player p : players) {
                    p.getDowncards().add(deck.dealCard());
                }
            }
            for (int i = 0; i < 3; i++) {
                for (Player p : players) {
                    p.getUpcards().add(deck.dealCard());
                }
            }
            for (int i = 0; i < 3; i++) {
                for (Player p : players) {
                    p.getHand().add(deck.dealCard());
                }
            }
        }
        catch (CardDeckEmptyException ex) {
            throw new IllegalStateException("not enough cards in deck for initial deal", ex);
        }
    }

    /**
     * Returns the player whose turn it is.
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayer);
    }

    /**
     * Plays a set of cards from the player's hand or table. The cards must all
     * be of the same rank and be valid according to the top of the waste pile.
     *
     * @param player player performing the play
     * @param cards  list of cards to play
     * @return {@code true} if the play was successful, {@code false} otherwise
     */
    public boolean playCards(Player player, List<Card> cards) {
        if (cards.isEmpty()) {
            return false;
        }

        // all cards must share rank
        Rank rank = cards.get(0).getRank();
        for (Card c : cards) {
            if (c.getRank() != rank) {
                return false;
            }
        }

        Card top = wastePile.isEmpty() ? null : wastePile.get(wastePile.size() - 1);
        if (!isPlayable(rank, top)) {
            return false;
        }

        // remove from player's locations
        for (Card c : cards) {
            if (player.getHand().remove(c)) {
                // removed from hand
            } else if (player.getUpcards().remove(c)) {
                // removed from table
            } else if (player.getDowncards().remove(c)) {
                // playing blind card
            }
            wastePile.add(c);
        }

        // apply special rules
        applySpecialRules(rank);

        refillHand(player);

        // check if player has emptied all cards -> remove them from game
        if (player.getHand().size() == 0 && player.getUpcards().isEmpty() && player.getDowncards().isEmpty()) {
            players.remove(player);
            if (currentPlayer >= players.size()) {
                currentPlayer = 0;
            }
        } else {
            advanceTurn();
        }

        return true;
    }

    private void applySpecialRules(Rank rank) {
        // Ten burns the pile
        if (rank == Rank.TEN) {
            wastePile.clear();
        }
        // Four of a kind burns the pile
        if (wastePile.size() >= 4) {
            int size = wastePile.size();
            Rank r1 = wastePile.get(size - 1).getRank();
            Rank r2 = wastePile.get(size - 2).getRank();
            Rank r3 = wastePile.get(size - 3).getRank();
            Rank r4 = wastePile.get(size - 4).getRank();
            if (r1 == r2 && r2 == r3 && r3 == r4) {
                wastePile.clear();
            }
        }
    }

    private boolean isPlayable(Rank rank, Card top) {
        if (rank == Rank.TWO || rank == Rank.TEN || top == null) {
            return true;
        }
        if (top.getRank() == Rank.TWO) {
            return true;
        }
        return rank.toInt() >= top.getRank().toInt();
    }

    private void advanceTurn() {
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    private void refillHand(Player player) {
        try {
            while (player.getHand().size() < 3) {
                player.getHand().add(deck.dealCard());
            }
        }
        catch (CardDeckEmptyException ex) {
            // this is fine, the deck is empty now
        }
    }

    /**
     * Returns the current waste pile as an unmodifiable list.
     */
    public List<Card> getWastePile() {
        return Collections.unmodifiableList(wastePile);
    }

    /**
     * Returns whether the game has finished. The last remaining player is the
     * loser of the game.
     */
    public boolean isFinished() {
        return players.size() <= 1;
    }
}
