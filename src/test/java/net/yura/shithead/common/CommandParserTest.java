package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    private ShitheadGame game;
    private CommandParser parser;
    private Player player1;

    @BeforeEach
    public void setUp() throws Exception {
        game = new ShitheadGame(1);
        parser = new CommandParser();
        player1 = game.getPlayers().get(0);

        // Clear all card piles for a clean slate
        player1.getHand().clear();
        player1.getUpcards().clear();
        player1.getDowncards().clear();
        game.setWastePile(new ArrayList<>());
        game.setPlayersReady(Set.of(player1));

        // Use reflection to set a predictable deck for testing
        Deck deck = game.getDeck();
        java.lang.reflect.Field field = Deck.class.getDeclaredField("cards");
        field.setAccessible(true);
        field.set(deck, new Stack<Card>());
    }

    @Test
    public void testPlayFromHand() {
        player1.getHand().add(Card.getCardByRankSuit(Rank.FIVE, Suit.HEARTS));
        parser.parse(game, "play hand 5H");
        assertEquals(1, game.getWastePile().size());
        assertEquals(0, player1.getHand().size());
    }

    @Test
    public void testPlayFromUpcards() {
        player1.getUpcards().add(Card.getCardByRankSuit(Rank.SIX, Suit.DIAMONDS));
        parser.parse(game, "play up 6D");
        assertEquals(1, game.getWastePile().size());
        assertEquals(0, player1.getUpcards().size());
    }

    @Test
    public void testPlayFromDowncards() {
        Card downCard = Card.getCardByRankSuit(Rank.SEVEN, Suit.CLUBS);
        player1.getDowncards().add(downCard);
        Card revealed = parser.parse(game, "play down 0");
        assertEquals(1, game.getWastePile().size());
        assertEquals(0, player1.getDowncards().size());
        assertEquals(downCard, revealed);
    }

    @Test
    public void testPickUpWastePile() {
        game.setWastePile(new java.util.ArrayList<>(Collections.singletonList(Card.getCardByRankSuit(Rank.EIGHT, Suit.SPADES))));
        parser.parse(game, "pickup");
        assertEquals(0, game.getWastePile().size());
        assertEquals(1, player1.getHand().size());
    }

    @Test
    public void testInvalidCommand() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(game, "fly away");
        });
    }

    @Test
    public void testPlayNonExistentCard() {
        player1.getHand().add(Card.getCardByRankSuit(Rank.FIVE, Suit.HEARTS));
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(game, "play hand 6h");
        });
    }

    @Test
    public void testInvalidMove() {
        game.setWastePile(new java.util.ArrayList<>(Collections.singletonList(Card.getCardByRankSuit(Rank.KING, Suit.HEARTS))));
        player1.getHand().add(Card.getCardByRankSuit(Rank.QUEEN, Suit.HEARTS));
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(game, "play hand qh");
        });
    }

    @Test
    public void testPlayRevealedDowncard() {
        Card downCard = Card.getCardByRankSuit(Rank.ACE, Suit.CLUBS);
        player1.getDowncards().add(downCard);

        // The parser should return the card that was played from the down pile
        Card revealedCard = parser.parse(game, "play down 0");

        // Assert that the returned card is the one we put in the down pile
        assertNotNull(revealedCard, "The revealed card should not be null");
        assertEquals(downCard, revealedCard, "The revealed card should be the Ace of Clubs");

        // Further assert game state
        assertTrue(game.getWastePile().contains(downCard), "The played card should be in the waste pile");
        assertFalse(player1.getDowncards().contains(downCard), "The played card should be removed from the downcards");
    }
}