package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
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
            java.lang.reflect.Field playersField = ShitheadGame.class.getDeclaredField("players");
            playersField.setAccessible(true);
            List<Player> players = (List<Player>) playersField.get(game);
            p1 = players.get(0);
            p2 = players.get(1);

            // Inject an empty deck to prevent hand refilling, which would break the test
            java.lang.reflect.Field deckField = ShitheadGame.class.getDeclaredField("deck");
            deckField.setAccessible(true);
            Deck emptyDeck = new Deck(1);
            while (true) {
                try {
                    emptyDeck.dealCard();
                } catch (Exception e) {
                    break; // deck is empty
                }
            }
            deckField.set(game, emptyDeck);

        } catch (Exception e) {
            fail("Failed to set up test with reflection: " + e.getMessage());
        }
    }

    @Test
    void testGameFlowAndWinCondition() {
        List<Card> deck = getFreshDeckCards();

        // Setup P1
        p1.getHand().clear();
        p1.getHand().add(findAndRemoveCard(deck, Rank.FIVE, Suit.SPADES));
        p1.getUpcards().clear();
        p1.getUpcards().add(findAndRemoveCard(deck, Rank.QUEEN, Suit.HEARTS));
        p1.getDowncards().clear();

        // Setup P2
        p2.getHand().clear();
        p2.getHand().add(findAndRemoveCard(deck, Rank.SIX, Suit.HEARTS));
        p2.getUpcards().clear();
        p2.getUpcards().add(findAndRemoveCard(deck, Rank.KING, Suit.CLUBS));
        p2.getDowncards().add(findAndRemoveCard(deck, Rank.JACK, Suit.DIAMONDS));

        // P1 plays from hand
        Card five = findCardInHand(p1.getHand(), Rank.FIVE);
        assertTrue(game.playCards(p1, Collections.singletonList(five)));
        assertTrue(p1.getHand().isEmpty());
        assertEquals(p2, game.getCurrentPlayer());

        // P2 plays from hand
        Card six = findCardInHand(p2.getHand(), Rank.SIX);
        assertTrue(game.playCards(p2, Collections.singletonList(six)));
        assertTrue(p2.getHand().isEmpty());
        assertEquals(p1, game.getCurrentPlayer());

        // P1 plays from upcards and wins
        Card queen = findCardInHand(p1.getUpcards(), Rank.QUEEN);
        assertTrue(game.playCards(p1, Collections.singletonList(queen)));
        assertTrue(p1.getUpcards().isEmpty());

        // Game should be finished as P1 has no cards left
        assertTrue(game.isFinished(), "Game should be finished after P1 plays their last upcard");
    }

    @Test
    void testPickUpWastePile() {
        List<Card> deck = getFreshDeckCards();
        p1.getHand().add(findAndRemoveCard(deck, Rank.KING, Suit.CLUBS));
        p1.getUpcards().add(findAndRemoveCard(deck, Rank.ACE, Suit.SPADES)); // Give p1 an upcard so they don't win

        // P1 plays a King
        game.playCards(p1, Collections.singletonList(findCardInHand(p1.getHand(), Rank.KING)));
        assertEquals(1, game.getWastePile().size());
        assertEquals(p2, game.getCurrentPlayer());

        // P2 has a 3, cannot play on a King
        p2.getHand().add(findAndRemoveCard(deck, Rank.THREE, Suit.DIAMONDS));
        assertFalse(game.playCards(p2, Collections.singletonList(findCardInHand(p2.getHand(), Rank.THREE))));

        // P2 picks up the pile
        int cardsInHandBefore = p2.getHand().size();
        int cardsInWasteBefore = game.getWastePile().size();
        game.pickUpWastePile(p2);

        assertEquals(cardsInHandBefore + cardsInWasteBefore, p2.getHand().size());
        assertEquals(0, game.getWastePile().size());
        assertEquals(p1, game.getCurrentPlayer());
    }

    @Test
    void testPlayFromUpcards() {
        p1.getHand().clear();
        List<Card> deck = getFreshDeckCards();
        Card queen = findAndRemoveCard(deck, Rank.QUEEN, Suit.HEARTS);
        p1.getUpcards().add(queen);

        // P1 plays their upcard
        assertTrue(game.playCards(p1, Collections.singletonList(queen)));
        assertTrue(p1.getUpcards().isEmpty());
        assertEquals(1, game.getWastePile().size());
    }

    @Test
    void testPlayFromDowncards() {
        p1.getHand().clear();
        p1.getUpcards().clear();
        List<Card> deck = getFreshDeckCards();
        Card ace = findAndRemoveCard(deck, Rank.ACE, Suit.DIAMONDS);
        p1.getDowncards().add(ace);

        // P1 plays their downcard (blind)
        assertTrue(game.playCards(p1, Collections.singletonList(ace)));
        assertTrue(p1.getDowncards().isEmpty());
        assertEquals(1, game.getWastePile().size());
    }

    @Test
    void testPlayTwoOnAnyCard() {
        List<Card> deck = getFreshDeckCards();
        Card king = findAndRemoveCard(deck, Rank.KING, Suit.SPADES);
        p1.getHand().add(king);
        game.playCards(p1, Collections.singletonList(king));

        Card two = findAndRemoveCard(deck, Rank.TWO, Suit.CLUBS);
        p2.getHand().add(two);

        // P2 can play a 2 on a King
        assertTrue(game.playCards(p2, Collections.singletonList(two)));
        assertEquals(2, game.getWastePile().size());
        assertEquals(Rank.TWO, game.getWastePile().get(1).getRank());
    }

    private List<Card> getFreshDeckCards() {
        Deck deck = new Deck(1);
        List<Card> cards = new ArrayList<>(52);
        try {
            for (int i = 0; i < 52; i++) {
                cards.add(deck.dealCard());
            }
        } catch (Exception e) {
            fail("Should be able to deal 52 cards from a fresh deck");
        }
        return cards;
    }

    private Card findAndRemoveCard(List<Card> cards, Rank rank, Suit suit) {
        Card found = null;
        for (Card card : cards) {
            if (card.getRank() == rank && card.getSuit() == suit) {
                found = card;
                break;
            }
        }
        if (found != null) {
            cards.remove(found);
            return found;
        }
        fail("Card not found: " + rank + " of " + suit);
        return null;
    }

    private Card findCardInHand(List<Card> hand, Rank rank) {
        return hand.stream()
                .filter(c -> c.getRank() == rank)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Card with rank " + rank + " not found in hand"));
    }
}