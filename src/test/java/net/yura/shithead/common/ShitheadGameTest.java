package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.CardDeckEmptyException;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

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
            java.lang.reflect.Field playersField = ShitheadGame.class.getDeclaredField("players");
            playersField.setAccessible(true);
            List<Player> players = (List<Player>) playersField.get(game);
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
        // Setup P1
        p1.getHand().add(Card.getCardByRankSuit(Rank.FIVE, Suit.SPADES));
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.QUEEN, Suit.HEARTS));

        // Setup P2
        p2.getHand().add(Card.getCardByRankSuit(Rank.SIX, Suit.HEARTS));
        p2.getUpcards().add(Card.getCardByRankSuit(Rank.KING, Suit.CLUBS));
        p2.getDowncards().add(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS));

        // P1 plays from hand
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.FIVE, Suit.SPADES))));
        assertTrue(p1.getHand().isEmpty());
        assertEquals(p2, game.getCurrentPlayer());

        // P2 plays from hand
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.SIX, Suit.HEARTS))));
        assertTrue(p2.getHand().isEmpty());
        assertEquals(p1, game.getCurrentPlayer());

        // P1 plays from upcards and wins
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.QUEEN, Suit.HEARTS))));
        assertTrue(p1.getUpcards().isEmpty());
        assertTrue(game.isFinished(), "Game should be finished after P1 plays their last upcard");
    }

    @Test
    void testPickUpWastePile() {
        p1.getHand().add(Card.getCardByRankSuit(Rank.KING, Suit.CLUBS));
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.ACE, Suit.SPADES)); // Ensure p1 doesn't win immediately

        // P1 plays a King
        game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.KING, Suit.CLUBS)));
        assertEquals(1, game.getWastePile().size());
        assertEquals(p2, game.getCurrentPlayer());

        // P2 has a 3, cannot play on a King
        p2.getHand().add(Card.getCardByRankSuit(Rank.THREE, Suit.DIAMONDS));
        assertFalse(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.THREE, Suit.DIAMONDS))));

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
        p1.getDowncards().add(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS));

        // P1 plays their downcard (blind)
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.ACE, Suit.DIAMONDS))));
        assertTrue(p1.getDowncards().isEmpty());
        assertEquals(1, game.getWastePile().size());
    }

    @Test
    void testPlaySpecialCards() {
        // Give players upcards so they don't win immediately
        p1.getUpcards().add(Card.getCardByRankSuit(Rank.ACE, Suit.HEARTS));
        p2.getUpcards().add(Card.getCardByRankSuit(Rank.ACE, Suit.CLUBS));

        // Test playing a Two on any card
        p1.getHand().add(Card.getCardByRankSuit(Rank.KING, Suit.SPADES));
        game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.KING, Suit.SPADES)));

        p2.getHand().add(Card.getCardByRankSuit(Rank.TWO, Suit.CLUBS));
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.TWO, Suit.CLUBS))));
        assertEquals(2, game.getWastePile().size());

        // Test playing a Ten to burn the pile and play again
        p1.getHand().add(Card.getCardByRankSuit(Rank.TEN, Suit.DIAMONDS));
        assertTrue(game.playCards(Collections.singletonList(Card.getCardByRankSuit(Rank.TEN, Suit.DIAMONDS))));
        assertEquals(0, game.getWastePile().size());
        assertEquals(p1, game.getCurrentPlayer(), "Player should play again after burning the pile");
    }
}