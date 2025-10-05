package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.shithead.common.json.SerializerUtil;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShitheadGameSerializationTest {

    @Test
    public void testFromJson() throws IOException, NoSuchFieldException, IllegalAccessException {
        // This is a snapshot of a game state, generated from a predictably shuffled deck.
        // Using a hardcoded string makes the test independent of the serialization logic
        // and ensures that fromJSON is tested against a stable, known input.
        String jsonSnapshot = "{\n" +
                "  \"currentPlayerName\" : \"p1\",\n" +
                "  \"deck\" : [ \"5C\", \"9S\", \"3S\", \"QC\", \"XC\", \"5S\", \"7H\", \"3H\", \"KH\", \"XD\", \"6C\", \"AD\", \"JS\", \"2D\", \"9D\", \"7C\", \"4D\", \"8S\", \"2H\", \"3D\", \"KD\", \"KC\", \"8C\", \"6H\", \"AS\", \"XH\", \"4S\", \"AH\", \"JC\", \"4H\", \"JH\", \"7S\", \"JD\", \"KS\" ],\n" +
                "  \"wastePile\" : [ ],\n" +
                "  \"players\" : [ {\n" +
                "    \"name\" : \"p1\",\n" +
                "    \"upcards\" : [ \"5H\", \"6S\", \"8D\" ],\n" +
                "    \"hand\" : [ \"2S\", \"QD\", \"QS\" ],\n" +
                "    \"downcards\" : [ \"9H\", \"AC\", \"6D\" ]\n" +
                "  }, {\n" +
                "    \"name\" : \"p2\",\n" +
                "    \"upcards\" : [ \"9C\", \"7D\", \"XS\" ],\n" +
                "    \"hand\" : [ \"3C\", \"8H\", \"2C\" ],\n" +
                "    \"downcards\" : [ \"5D\", \"4C\", \"QH\" ]\n" +
                "  } ]\n" +
                "}";

        // Deserialize the JSON snapshot back to a game object
        ShitheadGame deserializedGame = SerializerUtil.fromJSON(jsonSnapshot);

        // Assert that the deserialized game state is identical to the snapshot
        assertEquals("p1", deserializedGame.getCurrentPlayer().getName());
        assertTrue(deserializedGame.getWastePile().isEmpty());
        assertEquals(2, deserializedGame.getPlayers().size());

        // Assert player 1 state
        Player p1 = deserializedGame.getPlayers().get(0);
        assertEquals("p1", p1.getName());
        assertCardListsEqual(Arrays.asList("2S", "QD", "QS"), p1.getHand());
        assertCardListsEqual(Arrays.asList("5H", "6S", "8D"), p1.getUpcards());
        assertCardListsEqual(Arrays.asList("9H", "AC", "6D"), p1.getDowncards());

        // Assert player 2 state
        Player p2 = deserializedGame.getPlayers().get(1);
        assertEquals("p2", p2.getName());
        assertCardListsEqual(Arrays.asList("3C", "8H", "2C"), p2.getHand());
        assertCardListsEqual(Arrays.asList("9C", "7D", "XS"), p2.getUpcards());
        assertCardListsEqual(Arrays.asList("5D", "4C", "QH"), p2.getDowncards());

        // Assert deck state
        List<String> expectedDeck = Arrays.asList("5C", "9S", "3S", "QC", "XC", "5S", "7H", "3H", "KH", "XD", "6C", "AD", "JS", "2D", "9D", "7C", "4D", "8S", "2H", "3D", "KD", "KC", "8C", "6H", "AS", "XH", "4S", "AH", "JC", "4H", "JH", "7S", "JD", "KS");
        assertCardListsEqual(expectedDeck, getDeckCards(deserializedGame.getDeck()));
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

    private void assertCardListsEqual(List<String> expected, List<Card> actual) {
        List<String> actualStrings = actual.stream().map(Card::toString).collect(Collectors.toList());
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actualStrings.get(i));
        }
    }
}