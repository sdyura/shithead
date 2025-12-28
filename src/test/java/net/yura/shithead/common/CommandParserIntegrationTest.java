package net.yura.shithead.common;

import net.yura.cardsengine.Deck;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandParserIntegrationTest {

    @Test
    public void testSpectatorGame() throws Exception {
        // Setup a game to a certain point
        CommandParser parser = new CommandParser();
        ShitheadGame game = new ShitheadGame(2);
        Deck deck = game.getDeck();
        deck.setRandom(new Random(123)); // Fixed seed
        game.deal();

        String[] setupCommands = {
            "ready Player+1", "ready Player+2",
            "play hand 7D", "play hand 2C", "play hand QS", "play hand KC", "pickup", "play hand JC"
        };

        for (String command : setupCommands) {
            parser.parse(game, command);
        }

        // Serialize from a spectator's POV
        String spectatorJson = net.yura.shithead.common.json.SerializerUtil.toJSON(game, "spectator");

        // Deserialize back
        ShitheadGame spectatorGame = net.yura.shithead.common.json.SerializerUtil.fromJSON(spectatorJson);

        // Continue the game with the spectator's view
        String[] subsequentCommands = {
            "play hand 2C", "play hand 8C", "play hand 9S", "pickup", "play hand 7C", "play hand 7S"
        };

        for (String command : subsequentCommands) {
            parser.parse(spectatorGame, command);
        }

        assertEquals("Player 1", spectatorGame.getCurrentPlayer().getName());
    }
}
