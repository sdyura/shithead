package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import net.yura.shithead.common.json.SerializerUtil;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShitheadGameSerializationTest {

    @Test
    public void testFromJson() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Create a game with a predictable state
        List<String> playerNames = Arrays.asList("p1", "p2");
        Deck deck = new Deck(0); // Create an empty deck
        Stack<Card> cards = new Stack<>();
        // Add enough cards for a 2-player deal (18 cards needed) plus some left over
        cards.push(Card.getCardByRankSuit(Rank.ACE, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.TWO, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.THREE, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.FOUR, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.FIVE, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.SIX, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.SEVEN, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.EIGHT, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.NINE, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.TEN, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.JACK, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.QUEEN, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.KING, Suit.SPADES));
        cards.push(Card.getCardByRankSuit(Rank.ACE, Suit.HEARTS));
        cards.push(Card.getCardByRankSuit(Rank.TWO, Suit.HEARTS));
        cards.push(Card.getCardByRankSuit(Rank.THREE, Suit.HEARTS));
        cards.push(Card.getCardByRankSuit(Rank.FOUR, Suit.HEARTS));
        cards.push(Card.getCardByRankSuit(Rank.FIVE, Suit.HEARTS));
        cards.push(Card.getCardByRankSuit(Rank.SIX, Suit.HEARTS));
        setDeckCards(deck, cards);

        ShitheadGame originalGame = new ShitheadGame(playerNames, deck);
        originalGame.deal();

        // Serialize the game to JSON
        String json = SerializerUtil.toJSON(originalGame, null);

        // Deserialize the JSON back to a game object
        ShitheadGame deserializedGame = SerializerUtil.fromJSON(json);

        // Assert that the deserialized game state is identical to the original
        assertEquals(originalGame.getCurrentPlayer().getName(), deserializedGame.getCurrentPlayer().getName());
        assertCardListsEqual(getDeckCards(originalGame.getDeck()), getDeckCards(deserializedGame.getDeck()));
        assertCardListsEqual(originalGame.getWastePile(), deserializedGame.getWastePile());
        assertEquals(originalGame.getPlayers().size(), deserializedGame.getPlayers().size());

        for (int i = 0; i < originalGame.getPlayers().size(); i++) {
            Player originalPlayer = originalGame.getPlayers().get(i);
            Player deserializedPlayer = deserializedGame.getPlayers().get(i);
            assertEquals(originalPlayer.getName(), deserializedPlayer.getName());
            assertCardListsEqual(originalPlayer.getHand(), deserializedPlayer.getHand());
            assertCardListsEqual(originalPlayer.getUpcards(), deserializedPlayer.getUpcards());
            assertCardListsEqual(originalPlayer.getDowncards(), deserializedPlayer.getDowncards());
        }
    }

    private void setDeckCards(Deck deck, Stack<Card> cards) throws NoSuchFieldException, IllegalAccessException {
        // The Deck class from the external cardsengine.jar library does not provide a
        // public method to construct a deck with a specific set of cards. Reflection
        // is used here as a workaround to set up a predictable deck for testing.
        Field cardsField = Deck.class.getDeclaredField("cards");
        cardsField.setAccessible(true);
        cardsField.set(deck, cards);
    }

    private List<Card> getDeckCards(Deck deck) throws NoSuchFieldException, IllegalAccessException {
        Field cardsField = Deck.class.getDeclaredField("cards");
        cardsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Stack<Card> cards = (Stack<Card>) cardsField.get(deck);
        return cards;
    }

    private void assertCardListsEqual(List<Card> expected, List<Card> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).toString());
        }
    }
}