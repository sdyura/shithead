package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.shithead.common.json.SerializerUtil;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShitheadGameSerializationTest {

    @Test
    public void testFromJson() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Create a game with a predictable state
        List<String> playerNames = Arrays.asList("p1", "p2");
        // Use a seeded random to ensure the deck is shuffled predictably for testing
        Deck deck = new Deck(1);
        deck.setRandom(new Random(12345));
        deck.shuffle();

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

    private List<Card> getDeckCards(Deck deck) throws NoSuchFieldException, IllegalAccessException {
        // The Deck class from the external cardsengine.jar library does not provide a
        // public method to get the list of cards. Reflection is used here as a
        // workaround to access the internal state of the deck for assertion purposes.
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