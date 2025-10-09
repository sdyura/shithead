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

    private List<Player> players = new ArrayList<>();
    private final Deck deck;
    private List<Card> wastePile = new ArrayList<>();

    private int currentPlayer;

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setWastePile(List<Card> wastePile) {
        this.wastePile = wastePile;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Creates a new game with the given number of players.
     *
     * @param playerCount number of players taking part
     */
    public ShitheadGame(int playerCount) {
        this(playerCount, new Deck(1));
    }

    /**
     * Creates a new game with the given number of players and a specific deck.
     * This is useful for testing with a predictable deck.
     *
     * @param playerCount number of players taking part
     * @param deck The deck to be used in the game.
     */
    public ShitheadGame(int playerCount, Deck deck) {
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
        this.deck = deck;
    }

    /**
     * Creates a new game with a list of named players.
     * @param playerNames list of player names
     */
    public ShitheadGame(List<String> playerNames) {
        this(playerNames, new Deck(1));
    }

    /**
     * Creates a new game with a list of named players and a specific deck.
     * @param playerNames list of player names
     * @param deck The deck to be used in the game.
     */
    public ShitheadGame(List<String> playerNames, Deck deck) {
        for (String name : playerNames) {
            players.add(new Player(name));
        }
        this.deck = deck;
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
     * Returns the list of players in the game.
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Plays a set of cards from the player's hand or table. The cards must all
     * be of the same rank and be valid according to the top of the waste pile.
     *
     * @param cards list of cards to play
     * @return {@code true} if the play was successful, {@code false} otherwise
     */
    public boolean playCards(List<Card> cards) {
        Player player = getCurrentPlayer();

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

        List<Card> sourcePile;
        if (!player.getHand().isEmpty()) {
            sourcePile = player.getHand();
        } else if (!player.getUpcards().isEmpty()) {
            sourcePile = player.getUpcards();
        } else if (!player.getDowncards().isEmpty()) {
            sourcePile = player.getDowncards();
        } else {
            return false; // Player has no cards.
        }

        if (!sourcePile.containsAll(cards)) {
            return false;
        }

        Card top = wastePile.isEmpty() ? null : wastePile.get(wastePile.size() - 1);
        if (!isPlayable(rank, top)) {
            if (sourcePile == player.getDowncards()) {
                // Penalty for invalid down-card play
                sourcePile.removeAll(cards);
                player.getHand().addAll(cards);
                pickUpWastePile();
            }
            return false;
        }

        // remove from player's locations
        sourcePile.removeAll(cards);
        wastePile.addAll(cards);

        // apply special rules
        boolean burned = applySpecialRules(rank);

        refillHand(player);

        boolean playerWon = player.getHand().isEmpty() && player.getUpcards().isEmpty() && player.getDowncards().isEmpty();

        if (playerWon) {
            players.remove(player);
            if (currentPlayer >= players.size()) {
                currentPlayer = 0;
            }
        } else if (!burned) {
            advanceTurn();
        }

        return true;
    }

    /**
     * The current player picks up all the cards from the waste pile.
     * This is usually done when the player cannot play any of their cards.
     * After picking up the pile, the turn advances to the next player.
     */
    public void pickUpWastePile() {
        Player player = getCurrentPlayer();
        player.getHand().addAll(wastePile);
        wastePile.clear();
        advanceTurn();
    }

    private boolean applySpecialRules(Rank rank) {
        boolean burned = false;
        // Ten burns the pile
        if (rank == Rank.TEN) {
            wastePile.clear();
            burned = true;
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
                burned = true;
            }
        }
        return burned;
    }

    public boolean isPlayable(Rank rank, Card top) {
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

    public Deck getDeck() {
        return deck;
    }
}
