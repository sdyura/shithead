package net.yura.shithead.common.json;

import net.yura.cardsengine.Deck;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.common.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializerUtilTest {

    private ShitheadGame game;

    // Snapshot of the game state from Alice's perspective.
    private static final String ALICE_JSON_SNAPSHOT = "{\n" +
            "  \"currentPlayerName\" : \"Alice\",\n" +
            "  \"cardsInDeck\" : 34,\n" +
            "  \"wastePile\" : [ ],\n" +
            "  \"players\" : [ {\n" +
            "    \"name\" : \"Alice\",\n" +
            "    \"upcards\" : [ \"6H\", \"3C\", \"JH\" ],\n" +
            "    \"hand\" : [ \"7D\", \"QS\", \"9S\" ],\n" +
            "    \"downcardsCount\" : 3\n" +
            "  }, {\n" +
            "    \"name\" : \"Bob\",\n" +
            "    \"upcards\" : [ \"XH\", \"3S\", \"KD\" ],\n" +
            "    \"handCount\" : 3,\n" +
            "    \"downcardsCount\" : 3\n" +
            "  } ]\n" +
            "}";

    @BeforeEach
    public void setUp() {
        Deck deck = new Deck(1);
        deck.setRandom(new Random(123)); // Use a fixed seed for predictable tests
        game = new ShitheadGame(Arrays.asList("Alice", "Bob"), deck);
        game.deal();
    }

    @Test
    public void testFullGameSerializationMatchesSnapshot() throws Exception {
        String expectedJson = TestUtil.loadResourceAsString("/testgame.json");
        String generatedJson = SerializerUtil.toJSON(game, null);
        assertEquals(expectedJson, generatedJson.replaceAll("\\r\\n", "\n"));
    }

    @Test
    public void testPlayerSpecificSerializationMatchesSnapshot() throws Exception {
        String generatedJson = SerializerUtil.toJSON(game, "Alice");
        assertEquals(ALICE_JSON_SNAPSHOT, generatedJson.replaceAll("\\r\\n", "\n"));
    }
}