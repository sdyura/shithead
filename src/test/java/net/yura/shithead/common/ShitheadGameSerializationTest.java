package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.shithead.common.json.SerializerUtil;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShitheadGameSerializationTest {

    private String loadResourceAsString(String path) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    @Test
    public void testFromJson() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Load the game state snapshot from a shared test resource file.
        String jsonSnapshot = loadResourceAsString("/testgame.json");

        // Deserialize the JSON snapshot back to a game object
        ShitheadGame deserializedGame = SerializerUtil.fromJSON(jsonSnapshot);

        // Assert that the deserialized game state is identical to the snapshot
        assertEquals("Alice", deserializedGame.getCurrentPlayer().getName());
        assertTrue(deserializedGame.getWastePile().isEmpty());
        assertEquals(2, deserializedGame.getPlayers().size());

        // Assert player 1 state
        Player p1 = deserializedGame.getPlayers().get(0);
        assertEquals("Alice", p1.getName());
        assertCardListsEqual(Arrays.asList("7D", "QS", "9S"), p1.getHand());
        assertCardListsEqual(Arrays.asList("6H", "3C", "JH"), p1.getUpcards());
        assertCardListsEqual(Arrays.asList("5D", "AD", "2D"), p1.getDowncards());

        // Assert player 2 state
        Player p2 = deserializedGame.getPlayers().get(1);
        assertEquals("Bob", p2.getName());
        assertCardListsEqual(Arrays.asList("2C", "JC", "8C"), p2.getHand());
        assertCardListsEqual(Arrays.asList("XH", "3S", "KD"), p2.getUpcards());
        assertCardListsEqual(Arrays.asList("9C", "9H", "AS"), p2.getDowncards());

        // Assert deck state
        List<String> expectedDeck = Arrays.asList("XD", "QD", "6D", "6S", "4H", "5C", "7H", "4C", "6C", "4S", "8D", "KS", "3D", "AH", "XC", "XS", "QH", "9D", "QC", "JS", "KH", "3H", "8S", "2S", "JD", "8H", "5H", "2H", "5S", "7S", "AC", "4D", "KC", "7C");
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