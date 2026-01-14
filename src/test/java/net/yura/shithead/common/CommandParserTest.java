package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    private ShitheadGame game;
    private CommandParser parser;
    private Player player1;

    @BeforeEach
    public void setUp() throws Exception {
        // need minimum 2 players otherwise the game is over
        game = new ShitheadGame(2);
        parser = new CommandParser();
        player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);

        // Clear all card piles for a clean slate
        player1.getHand().clear();
        player1.getUpcards().clear();
        player1.getDowncards().clear();
        game.setWastePile(new ArrayList<>());
        game.setPlayersReady(Set.of(player1, player2));
        game.setCurrentPlayer(0);

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
        String mutation = CommandParser.getMutationCommand(game, "play down 0");
        assertEquals("play down " + downCard, mutation);

        parser.execute(game, mutation);
        assertEquals(1, game.getWastePile().size());
        assertEquals(0, player1.getDowncards().size());

        assertTrue(game.getWastePile().contains(downCard), "The played card should be in the waste pile");
        assertFalse(player1.getDowncards().contains(downCard), "The played card should be removed from the downcards");
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
    public void testRearrangeCards() {
        // --- Setup: Place specific cards in hand and up-cards ---
        Card handCard = Card.getCardByRankSuit(Rank.ACE, Suit.SPADES);
        Card upCard = Card.getCardByRankSuit(Rank.KING, Suit.HEARTS);
        player1.getHand().add(handCard);
        player1.getUpcards().add(upCard);

        // In order to test the "rearrange" command, the game must be in the "rearranging" state.
        // The setUp() method puts the game into the "playing" state, so we need to reset it here.
        game.setPlayersReady(new HashSet<>());

        // --- Action: Execute the rearrange command ---
        // Command format: rearrange <player_name> <hand_card> <up_card>
        parser.execute(game, "swap Player%201 AS KH");

        // --- Verification: Check if cards were swapped ---
        assertFalse(player1.getHand().contains(handCard), "The hand card should no longer be in the hand.");
        assertTrue(player1.getUpcards().contains(handCard), "The hand card should now be in the up-cards.");
        assertTrue(player1.getHand().contains(upCard), "The up-card should now be in the hand.");
        assertFalse(player1.getUpcards().contains(upCard), "The up-card should no longer be in the up-cards.");
    }
}