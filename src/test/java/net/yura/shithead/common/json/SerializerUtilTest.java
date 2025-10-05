package net.yura.shithead.common.json;

import net.yura.cardsengine.Deck;
import net.yura.shithead.common.ShitheadGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializerUtilTest {

    private ShitheadGame game;

    // Snapshot of the full game state with a predictable seed.
    private static final String FULL_JSON_SNAPSHOT = "{\n" +
            "  \"currentPlayerName\" : \"Alice\",\n" +
            "  \"deck\" : [ \"XD\", \"QD\", \"6D\", \"6S\", \"4H\", \"5C\", \"7H\", \"4C\", \"6C\", \"4S\", \"8D\", \"KS\", \"3D\", \"AH\", \"XC\", \"XS\", \"QH\", \"9D\", \"QC\", \"JS\", \"KH\", \"3H\", \"8S\", \"2S\", \"JD\", \"8H\", \"5H\", \"2H\", \"5S\", \"7S\", \"AC\", \"4D\", \"KC\", \"7C\" ],\n" +
            "  \"wastePile\" : [ ],\n" +
            "  \"players\" : [ {\n" +
            "    \"name\" : \"Alice\",\n" +
            "    \"upcards\" : [ \"6H\", \"3C\", \"JH\" ],\n" +
            "    \"hand\" : [ \"7D\", \"QS\", \"9S\" ],\n" +
            "    \"downcards\" : [ \"5D\", \"AD\", \"2D\" ]\n" +
            "  }, {\n" +
            "    \"name\" : \"Bob\",\n" +
            "    \"upcards\" : [ \"XH\", \"3S\", \"KD\" ],\n" +
            "    \"hand\" : [ \"2C\", \"JC\", \"8C\" ],\n" +
            "    \"downcards\" : [ \"9C\", \"9H\", \"AS\" ]\n" +
            "  } ]\n" +
            "}";

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
        String generatedJson = SerializerUtil.toJSON(game, null);
        assertEquals(FULL_JSON_SNAPSHOT, generatedJson.replaceAll("\\r\\n", "\n"));
    }

    @Test
    public void testPlayerSpecificSerializationMatchesSnapshot() throws Exception {
        String generatedJson = SerializerUtil.toJSON(game, "Alice");
        assertEquals(ALICE_JSON_SNAPSHOT, generatedJson.replaceAll("\\r\\n", "\n"));
    }
}