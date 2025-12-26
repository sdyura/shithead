package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.CardDeckEmptyException;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import static org.junit.jupiter.api.Assertions.*;

class ShitheadGameTest {

    private ShitheadGame game;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        game = new ShitheadGame(2);
        try {
            // Get players for easier testing
            List<Player> players = game.getPlayers();
            p1 = players.get(0);
            p2 = players.get(1);

            // Inject an empty deck to prevent hand refilling, which would break the tests
            java.lang.reflect.Field deckField = ShitheadGame.class.getDeclaredField("deck");
            deckField.setAccessible(true);
            Deck emptyDeck = new Deck(1);
            try {
                while (true) {
                    emptyDeck.dealCard();
                }
            } catch (CardDeckEmptyException e) {
                // Deck is now empty, which is what we want.
            }
            deckField.set(game, emptyDeck);

        } catch (Exception e) {
            fail("Failed to set up test with reflection: " + e.getMessage());
        }
    }

    @Test
    void testGameFlowAndWinCondition() {
        // All players are ready
        game.playerReady(p1);
        game.playerReady(p2);

        // Setup P1 to start
        p1.getHand().add(Card.getCardByRankSuit(Rank.THREE, Suit.SPADES));
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.QUEEN, Suit.HEARTS));

        // Setup P2
        p2.getHand().add(Card.getCardByRankSuit(Rank.FOUR, Suit.HEARTS));
        p2.getUpcards().add(Card.getCardByRankSuit(Rank.KING, Suit.CLUBS));
        p2.getDowncards().add(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS));

        // P1 plays from hand
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.THREE, Suit.SPADES))));
        assertTrue(p1.getHand().isEmpty());
        assertEquals(p2, game.getCurrentPlayer());

        // P2 plays from hand
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.FOUR, Suit.HEARTS))));
        assertTrue(p2.getHand().isEmpty());
        assertEquals(p1, game.getCurrentPlayer());

        // P1 plays from upcards and wins
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.QUEEN, Suit.HEARTS))));
        assertTrue(p1.getUpcards().isEmpty());
        assertTrue(game.isFinished(), "Game should be finished after P1 plays their last upcard");
    }

    @Test
    void testPickUpWastePile() {
        // Setup so P1 plays a King and P2 must pick it up.
        // P1 must start
        p1.getHand().add(Card.getCardByRankSuit(Rank.THREE, Suit.CLUBS)); // To start
        p1.getHand().add(Card.getCardByRankSuit(Rank.KING, Suit.CLUBS));
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.ACE, Suit.SPADES));

        // P2 has a high card and an unplayable card
        p2.getHand().add(Card.getCardByRankSuit(Rank.FOUR, Suit.HEARTS)); // to not start
        p2.getHand().add(Card.getCardByRankSuit(Rank.SIX, Suit.DIAMONDS));

        game.playerReady(p1);
        game.playerReady(p2); // P1 starts.

        // P1 plays the 3.
        game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.THREE, Suit.CLUBS)));
        // P2's turn. Plays the 4.
        game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.FOUR, Suit.HEARTS)));
        // P1's turn. Plays the King.
        game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.KING, Suit.CLUBS)));

        // P2's turn. Top card is King. P2 has a 6. Cannot play.
        assertFalse(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.SIX, Suit.DIAMONDS))));

        // P2 picks up the pile
        int cardsInHandBefore = p2.getHand().size();
        int cardsInWasteBefore = game.getWastePile().size();
        game.pickUpWastePile();

        assertEquals(cardsInHandBefore + cardsInWasteBefore, p2.getHand().size());
        assertEquals(0, game.getWastePile().size());
        assertEquals(p1, game.getCurrentPlayer());
    }

    @Test
    void testPlayFromDowncards() {
        // Ensure hands and upcards are empty for both players
        p1.getHand().clear();
        p1.getUpcards().clear();
        p2.getHand().clear();
        p2.getUpcards().clear();

        p1.getDowncards().add(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS));
        p2.getDowncards().add(Card.getCardByRankSuit(Rank.KING, Suit.HEARTS));


        // All players are ready
        game.playerReady(p1);
        game.playerReady(p2);
        // Since hand and upcards are empty, the first player will be p1 (index 0) by default.
        // No need to set it manually.

        // P1 plays their downcard (blind)
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS))));
        assertTrue(p1.getDowncards().isEmpty());
        assertEquals(1, game.getWastePile().size());
    }

    @Test
    void testPlaySpecialCards() {
        // All players are ready
        game.playerReady(p1);
        game.playerReady(p2);

        // Give players upcards so they don't win immediately
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.ACE, Suit.HEARTS));
        p2.getUpcards().add(Card.getCardByRankSuit(Rank.ACE, Suit.CLUBS));

        // Test playing a Two on any card
        p1.getHand().add(Card.getCardByRankSuit(Rank.THREE, Suit.SPADES));
        p2.getHand().add(Card.getCardByRankSuit(Rank.FOUR, Suit.HEARTS));
        game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.THREE, Suit.SPADES)));

        p2.getHand().add(Card.getCardByRankSuit(Rank.TWO, Suit.CLUBS));
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.TWO, Suit.CLUBS))));
        assertEquals(2, game.getWastePile().size());

        // Test playing a Ten to burn the pile and play again
        p1.getHand().add(Card.getCardByRankSuit(Rank.TEN, Suit.DIAMONDS));
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.TEN, Suit.DIAMONDS))));
        assertEquals(0, game.getWastePile().size());
        assertEquals(p1, game.getCurrentPlayer(), "Player should play again after burning the pile");
    }

    @Test
    public void testPlayDeckCardSuccess() {
        // given
        Deck deck = new Deck(1);
        ShitheadGame game = new ShitheadGame(2, deck);
        game.deal();
        game.playerReady(game.getPlayers().get(0));
        game.playerReady(game.getPlayers().get(1));
        Player originalPlayer = game.getCurrentPlayer();
        Player otherPlayer = game.getPlayers().stream().filter(p -> p != originalPlayer).findFirst().get();

        Vector cards = deck.getCards();
        Stack stack = new Stack();
        stack.addAll(cards);
        // Make sure the top card is playable
        stack.set(stack.size() - 1, Card.getCardByRankSuit(Rank.ACE, Suit.SPADES));
        cards.clear();
        cards.addAll(stack);

        Card topCard = (Card) deck.getCards().get(deck.getCards().size() - 1);
        int initialDeckSize = deck.getCards().size();

        // when
        Card playedCard = new CommandParser().parse(game, "play deck");

        // then
        assertEquals(topCard, playedCard);
        assertEquals(initialDeckSize - 1, deck.getCards().size());
        assertTrue(game.getWastePile().contains(topCard));
        assertEquals(otherPlayer, game.getCurrentPlayer());
    }

    @Test
    public void testPlayDeckCardFail() {
        // given
        ShitheadGame game = new ShitheadGame(2);
        game.deal();
        game.playerReady(game.getPlayers().get(0));
        game.playerReady(game.getPlayers().get(1));
        Player originalPlayer = game.getCurrentPlayer();
        Player otherPlayer = game.getPlayers().stream().filter(p -> p != originalPlayer).findFirst().get();

        // Make the top card of the deck unplayable
        game.setWastePile(new java.util.ArrayList<>(Collections.singletonList(Card.getCardByRankSuit(Rank.KING, Suit.SPADES))));
        Deck deck = game.getDeck();
        Vector cards = deck.getCards();
        Stack stack = new Stack();
        stack.addAll(cards);
        stack.set(stack.size() - 1, Card.getCardByRankSuit(Rank.THREE, Suit.CLUBS));
        cards.clear();
        cards.addAll(stack);

        Card topCard = (Card) deck.getCards().get(deck.getCards().size() - 1);
        int initialDeckSize = deck.getCards().size();
        int initialHandSize = originalPlayer.getHand().size();

        // when
        Card playedCard = new CommandParser().parse(game, "play deck");

        // then
        assertEquals(topCard, playedCard);
        assertEquals(initialDeckSize - 1, deck.getCards().size());
        assertEquals(0, game.getWastePile().size());
        assertEquals(initialHandSize + 2, originalPlayer.getHand().size()); // +1 for the card from the deck, +1 for the card from the waste pile
        assertTrue(originalPlayer.getHand().contains(topCard));
        assertEquals(otherPlayer, game.getCurrentPlayer());
    }

    @Test
    void testChooseFirstPlayer() {
        // P2 has lowest card in upcards
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.FOUR, Suit.CLUBS));
        p2.getUpcards().add(Card.getCardByRankSuit(Rank.THREE, Suit.DIAMONDS));
        game.playerReady(p1);
        game.playerReady(p2);
        assertEquals(p2, game.getCurrentPlayer());
    }

    @Test
    void testChooseFirstPlayerTie() {
        // Both players have a 3, P1 should be chosen as they are first in the list
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.THREE, Suit.CLUBS));
        p2.getUpcards().add(Card.getCardByRankSuit(Rank.THREE, Suit.DIAMONDS));
        game.playerReady(p1);
        game.playerReady(p2);
        assertEquals(p1, game.getCurrentPlayer());
    }

    @Test
    void testChooseFirstPlayerNoValidCards() {
        // No player has a valid starting card (rank >= 3)
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.TWO, Suit.CLUBS));
        p2.getHand().add(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS));
        game.playerReady(p1);
        game.playerReady(p2);
        assertEquals(p1, game.getCurrentPlayer()); // Should default to P1 (index 0)
    }
}
