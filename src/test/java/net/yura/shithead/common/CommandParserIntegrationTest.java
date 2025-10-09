package net.yura.shithead.common;

import net.yura.cardsengine.Deck;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandParserIntegrationTest {

    @Test
    public void testHardcoded2PlayerGame() {
        CommandParser parser = new CommandParser();
        ShitheadGame game = new ShitheadGame(2);
        Deck deck = game.getDeck();
        deck.setRandom(new Random(123)); // Fixed seed for predictable game
        game.deal();

        String[] commands = {
            "play hand 7D", "play hand 2C", "play hand QS", "play hand KC", "pickup", "play hand JC",
            "play hand 2C", "play hand 8C", "play hand 9S", "pickup", "play hand 7C", "play hand 7S",
            "play hand 7D", "play hand JC", "play hand QS", "play hand 2C", "play hand 4D",
            "play hand 5S", "play hand KC", "pickup", "play hand 2H", "play hand AC", "play hand 5H",
            "play hand 8C", "play hand 8H", "play hand 9S", "play hand JD", "play hand JC",
            "play hand 2S", "play hand 7C", "play hand 8S", "play hand QS", "play hand KH",
            "play hand 2C", "play hand 3H", "play hand 7S", "play hand JS", "play hand KC", "pickup",
            "play hand 7D", "play hand QC", "play hand XS", "play hand 4D", "play hand 9D",
            "play hand XC", "play hand 5S", "play hand QH", "play hand KS", "play hand 2H",
            "play hand AH", "play hand AC", "play hand 3D", "play hand 5H", "play hand 8D",
            "play hand 8C", "pickup", "play hand 8H", "play hand QH", "play hand 2S", "play hand 4S",
            "play hand 9S", "play hand KS", "play hand KH", "play hand 2H", "play hand JD", "pickup",
            "play hand JC", "play hand QH", "play hand QS", "play hand 2S", "play hand 7C",
            "play hand 8D", "play hand 8S", "play hand 8C", "play hand 2C", "play hand 6C",
            "play hand 7S", "play hand 8H", "play hand JS", "play hand KS", "play hand KC",
            "play hand KH", "pickup", "play hand 4C", "play hand 7H", "play hand 9S", "play hand JC",
            "play hand 2H", "play hand 3H", "play hand 5S", "play hand 5C", "play hand 5H",
            "play hand QH", "pickup", "play hand QS", "play hand 2H", "play hand 2S", "play hand AH",
            "play hand 7C", "play hand JD", "play hand 2C", "play hand AC", "play hand 8D",
            "play hand 9S", "play hand JS", "play hand JC", "play hand KS", "pickup", "play hand 8S",
            "play hand QH", "play hand KC", "play hand 2H", "play hand 8C", "play hand QS",
            "play hand KH", "play hand 2S", "play hand 6C", "play hand 7H", "play hand 7S",
            "play hand 7C", "play hand 8H", "play hand JD", "pickup", "play hand 3D", "play hand 4H",
            "play hand 4S", "play hand 6S", "play hand 2C", "play hand 6D", "play hand 8D",
            "play hand 8S", "play hand 9S", "play hand QH", "play hand KS", "play hand KC", "pickup",
            "play hand 2H", "play hand 4C", "play hand 8C", "play hand JS", "play hand QS",
            "play hand 2C", "play hand KH", "play hand KS", "play hand 2S", "play hand 3H",
            "play hand 6C", "play hand JC", "play hand JD", "play hand QH", "pickup", "play hand 5S",
            "play hand 7H", "play hand 8D", "play hand 8H", "play hand 8S", "play hand 2H",
            "play hand 5C", "play hand 7S", "play hand 9S", "play hand JS", "play hand KC",
            "play hand 2C", "play hand 5H", "play hand 7C", "pickup", "play hand 4C", "play hand 4H",
            "play hand 8C", "play hand 8D", "play hand QS", "play hand 2H", "play hand KH",
            "play hand KC", "play hand KS", "play hand 2C", "play hand 2S", "play hand AH",
            "play hand 3H", "play hand 3D", "play hand 6C", "play hand 6S", "play hand JC",
            "play hand JS", "play hand JD", "pickup", "play hand QH", "play hand QS", "play hand QD",
            "play hand 2H", "play hand XD", "play up 6H", "play hand 6D", "play up JH", "play hand KH",
            "pickup", "play hand AC", "play hand 6H", "play hand 7H", "play hand JH", "play hand KC",
            "play hand KH", "play hand KS", "pickup", "play hand 4S", "play hand 6D", "play hand 8H",
            "play hand JH", "play hand 2C", "play hand AC", "play hand 5S", "play hand 6H",
            "play hand 8S", "play hand KC", "play hand 2S", "play hand 7H", "play hand 7S",
            "play hand KH", "pickup", "play hand KS", "play hand 2C", "play up 3C", "play hand 5C",
            "play down 2", "play hand 9S", "play down 1", "play hand 5H", "play hand KS",
            "play hand KC", "play hand 2C", "play hand 7C", "play hand 2D", "play hand 4C",
            "play hand 5C", "play hand 8C", "play hand 9S", "play hand JC", "pickup", "play hand 4H",
            "play hand 5H", "play hand 8D", "play hand KS", "play hand 2S", "play hand AD",
            "play hand AH", "play hand 3C", "play hand 3H", "play hand KC", "play hand KH",
            "play hand 2C", "play hand 3D", "play hand 7C", "play hand JS", "play hand 2D",
            "play hand 6C", "play hand 8C", "play hand JD", "play hand JC", "play hand JH",
            "pickup", "play hand 6S", "play hand 9S", "pickup", "play hand 4C", "play hand 4S",
            "play hand 5C", "play hand 6D", "play hand 8D", "play hand 8H", "play hand KS",
            "pickup", "play hand 4H", "play hand 5S", "play hand 5H", "play hand 6H", "play hand 2S",
            "play hand AC", "play hand AD", "play hand 8S", "play hand KC", "play hand KS",
            "play hand KH", "pickup", "play hand AH", "play hand 7H", "play hand 2C", "play hand 7S",
            "play hand 7C", "play hand 9S", "play hand JS", "play hand 2S", "play hand 3C",
            "play hand 6S", "play hand 2D", "play hand 4C", "play hand 6C", "play hand 6D",
            "play hand 8C", "play hand 8D", "play hand JD", "play hand KC", "pickup", "play hand 4S",
            "play hand JC", "play hand KS", "play hand 2C", "play hand 5C", "play hand JH",
            "play hand KH", "play hand 2S", "play hand 8H", "play hand 9S", "pickup", "play hand 3H",
            "play hand 4H", "play hand 7H", "play hand 8S", "play hand JS", "play hand JC",
            "play hand 2D", "play hand 5S", "play hand 7S", "play hand KS", "play hand KC",
            "play hand 2C", "play hand 3D", "play hand 5H", "play hand 7C", "play hand JH",
            "play hand JD", "play hand KH", "pickup", "play hand 6H", "play hand 6S", "play hand 2S",
            "play hand AH", "play hand AC", "play hand 3C", "play hand 4S", "play hand 4C",
            "play hand 5C", "play hand 6C", "play hand 8H", "play hand 8C", "play hand 9S",
            "play hand JS", "pickup", "play hand 6D", "play hand 6H", "play hand 8D", "play hand 2S",
            "play hand 3H", "play hand 6S", "play hand 7H", "play hand 8H", "play hand 8S",
            "play hand 8C", "play hand JC", "play hand JS", "play hand 2D", "play hand AD",
            "play hand 4H", "play hand 4S", "play hand 5S", "play hand 5C", "play hand 7S",
            "play hand 9S", "play hand KS", "pickup", "play hand KC", "play hand 2S", "play hand 2C",
            "play hand AH", "play hand 3D", "play hand 3C", "play hand 5H", "play hand 6C",
            "play hand 7C", "play hand 8D", "play hand JH", "play hand JC", "play hand JD",
            "play hand JS", "play hand AC", "play hand KH", "play hand 2D", "play down 0"
        };

        for (String command : commands) {
            if (game.isFinished()) {
                fail("command not needed: " + command);
            }
            parser.parse(game, command);
        }

        assertTrue(game.isFinished(), "Game should be finished After the hardcoded sequence of moves.");
        assertEquals(1, game.getPlayers().size(), "There should be only one player left (the winner).");
    }
}