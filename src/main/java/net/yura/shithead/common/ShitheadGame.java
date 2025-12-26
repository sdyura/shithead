package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.CardDeckEmptyException;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic implementation of the Shithead card game rules.
 * <p>
 * This class manages the deck, waste pile and players while exposing
 * helper methods to play cards and progress the game. It aims to mirror
 * the description of the game found on its Wikipedia page.
 */
public class ShitheadGame {

    /**
     * map of player that is ready to there lowest card, this card will be used to decide who goes first.
     * Note! the lowest card for a player may be null!
     */
    private Set<Player> playersReady = new HashSet<>();
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
        if (players.stream().mapToInt(Player::getNoCards).sum() > 0) {
            throw new IllegalStateException("Cards have already been dealt.");
        }
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

    public void rearrangeCards(Player player, Card handCard, Card upCard) {
        if (!isRearranging()) {
            throw new IllegalStateException("Game is not in REARRANGING state.");
        }
        if (!player.getHand().contains(handCard)) {
            throw new IllegalArgumentException("Player does not have the specified card in their hand.");
        }
        if (!player.getUpcards().contains(upCard)) {
            throw new IllegalArgumentException("Player does not have the specified card in their upcards.");
        }
        player.getHand().remove(handCard);
        player.getUpcards().remove(upCard);
        player.getHand().add(upCard);
        player.getUpcards().add(handCard);
    }

    public void playerReady(Player player) {
        if (!isRearranging()) {
            throw new IllegalStateException("Game is not in REARRANGING state.");
        }
        playersReady.add(player);

        if (isPlaying()) {
            chooseFirstPlayer();
        }
    }

    /**
     * for this method to work we NEED each player to have the lowest card set in the hand
     */
    private void chooseFirstPlayer() {
        int bestRank = Integer.MAX_VALUE;

        // first find the best rank
        for (Player pc : playersReady) {
            List<Card> combined = getCombinedHandAndUpcards(pc);
            Card c = findLowestStartCard(combined);
            if (c != null) {
                int rank = c.getRank().toInt();
                if (rank < bestRank) {
                    bestRank = rank;
                }
            }
        }

        // if no one has a valid card, player 0 starts
        if (bestRank == Integer.MAX_VALUE) {
            currentPlayer = 0;
            return;
        }

        // now find the first player with that rank
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (playersReady.contains(p)) {
                List<Card> combined = getCombinedHandAndUpcards(p);
                for (Card c : combined) {
                    if (c.getRank().toInt() == bestRank) {
                        currentPlayer = i;
                        return;
                    }
                }
            }
        }
    }

    private List<Card> getCombinedHandAndUpcards(Player p) {
        List<Card> combined = new ArrayList<>();
        combined.addAll(p.getUpcards());
        combined.addAll(p.getHand());
        return combined;
    }

    public static Card findLowestStartCard(Collection<Card> cards) {
        int bestRank = Integer.MAX_VALUE;
        Card lowest = null;
        for (Card c : cards) {
            int rank = c.getRank().toInt();
            if (rank >= 3 && rank < bestRank) {
                bestRank = rank;
                lowest = c;
            }
        }
        return lowest;
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
        if (!isPlaying()) {
            throw new IllegalStateException("Game is not in PLAYING state.");
        }
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

        // --- Validation for card ownership (handles spectator mode) ---
        List<Card> pileCopy = new ArrayList<>(sourcePile);
        for (Card card : cards) {
            if (!pileCopy.remove(card)) { // Try to remove the specific card
                if (!pileCopy.remove(null)) { // If not found, try to remove a null placeholder
                    return false; // Player has neither the card nor a placeholder for it
                }
            }
        }

        // --- Validation for game rules ---
        Card top = wastePile.isEmpty() ? null : wastePile.get(wastePile.size() - 1);
        if (!isPlayable(rank, top)) {
            if (sourcePile == player.getDowncards()) {
                // Penalty for invalid down-card play
                // We must remove the card from downcards and add it to hand before picking up pile
                for (Card card : cards) {
                    if (!sourcePile.remove(card)) {
                        sourcePile.remove(null); // Known to succeed due to validation above
                    }
                }
                player.getHand().addAll(cards);
                pickUpWastePile();
            }
            return false;
        }

        // --- Execution: Remove cards from the actual source pile ---
        for (Card card : cards) {
            if (!sourcePile.remove(card)) {
                sourcePile.remove(null); // Known to succeed due to validation above
            }
        }
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

    public void renamePlayer(String oldName, String newName) {
        for (Player player : players) {
            if (player.getName().equals(oldName)) {
                player.setName(newName);
                return;
            }
        }
    }

    public void removePlayer(String name) {
        int index = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            players.remove(index);
            if (index < currentPlayer) {
                currentPlayer--;
            } else if (index == currentPlayer) {
                if (currentPlayer >= players.size()) {
                    currentPlayer = 0;
                }
            }
        }
    }

    public boolean isRearranging() {
        return playersReady.size() < players.size();
    }

    public boolean isPlaying() {
        return playersReady.size() >= players.size();
    }

    public Set<Player> getPlayersReady() {
        return playersReady;
    }

    public void setPlayersReady(Set<Player> playersReady) {
        this.playersReady = playersReady;
    }

    public Card getTopCardFromDeck() {
        List<Card> cards = deck.getCards();
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }

    public void playCardFromDeck(Card card) {
        if (!isPlaying()) {
            throw new IllegalStateException("Game is not in PLAYING state.");
        }
        Card topCard = getTopCardFromDeck();
        if (topCard == null || !topCard.equals(card)) {
            throw new IllegalArgumentException("Card is not the top card of the deck.");
        }
        try {
            deck.dealCard();
            Card top = wastePile.isEmpty() ? null : wastePile.get(wastePile.size() - 1);
            if (isPlayable(card.getRank(), top)) {
                wastePile.add(card);
                boolean burned = applySpecialRules(card.getRank());
                if (!burned) {
                    advanceTurn();
                }
            } else {
                getCurrentPlayer().getHand().add(card);
                pickUpWastePile();
            }
        } catch (CardDeckEmptyException e) {
            // Nothing happens, deck is empty
        }
    }
}
