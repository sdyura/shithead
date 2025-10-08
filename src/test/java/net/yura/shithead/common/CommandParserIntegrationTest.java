package net.yura.shithead.common;

import net.yura.cardsengine.Deck;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandParserIntegrationTest {

    @Test
    public void testHardcoded2PlayerGame() throws InvalidCommandException {
        CommandParser parser = new CommandParser();
        ShitheadGame game = new ShitheadGame(2);
        Deck deck = game.getDeck();
        deck.setRandom(new Random(123)); // Fixed seed for predictable game
        game.deal();

        String[] commands = {
            "play hand 7d", "play hand 2c", "play hand qs", "play hand kc", "pickup", "play hand jc",
            "play hand 2c", "play hand 8c", "play hand 9s", "pickup", "play hand 7c", "play hand 7s",
            "play hand 7d", "play hand jc", "play hand qs", "play hand 2c", "play hand 4d",
            "play hand 5s", "play hand kc", "pickup", "play hand 2h", "play hand ac", "play hand 5h",
            "play hand 8c", "play hand 8h", "play hand 9s", "play hand jd", "play hand jc",
            "play hand 2s", "play hand 7c", "play hand 8s", "play hand qs", "play hand kh",
            "play hand 2c", "play hand 3h", "play hand 7s", "play hand js", "play hand kc", "pickup",
            "play hand 7d", "play hand qc", "play hand xs", "play hand 4d", "play hand 9d",
            "play hand xc", "play hand 5s", "play hand qh", "play hand ks", "play hand 2h",
            "play hand ah", "play hand ac", "play hand 3d", "play hand 5h", "play hand 8d",
            "play hand 8c", "pickup", "play hand 8h", "play hand qh", "play hand 2s", "play hand 4s",
            "play hand 9s", "play hand ks", "play hand kh", "play hand 2h", "play hand jd", "pickup",
            "play hand jc", "play hand qh", "play hand qs", "play hand 2s", "play hand 7c",
            "play hand 8d", "play hand 8s", "play hand 8c", "play hand 2c", "play hand 6c",
            "play hand 7s", "play hand 8h", "play hand js", "play hand ks", "play hand kc",
            "play hand kh", "pickup", "play hand 4c", "play hand 7h", "play hand 9s", "play hand jc",
            "play hand 2h", "play hand 3h", "play hand 5s", "play hand 5c", "play hand 5h",
            "play hand qh", "pickup", "play hand qs", "play hand 2h", "play hand 2s", "play hand ah",
            "play hand 7c", "play hand jd", "play hand 2c", "play hand ac", "play hand 8d",
            "play hand 9s", "play hand js", "play hand jc", "play hand ks", "pickup", "play hand 8s",
            "play hand qh", "play hand kc", "play hand 2h", "play hand 8c", "play hand qs",
            "play hand kh", "play hand 2s", "play hand 6c", "play hand 7h", "play hand 7s",
            "play hand 7c", "play hand 8h", "play hand jd", "pickup", "play hand 3d", "play hand 4h",
            "play hand 4s", "play hand 6s", "play hand 2c", "play hand 6d", "play hand 8d",
            "play hand 8s", "play hand 9s", "play hand qh", "play hand ks", "play hand kc", "pickup",
            "play hand 2h", "play hand 4c", "play hand 8c", "play hand js", "play hand qs",
            "play hand 2c", "play hand kh", "play hand ks", "play hand 2s", "play hand 3h",
            "play hand 6c", "play hand jc", "play hand jd", "play hand qh", "pickup", "play hand 5s",
            "play hand 7h", "play hand 8d", "play hand 8h", "play hand 8s", "play hand 2h",
            "play hand 5c", "play hand 7s", "play hand 9s", "play hand js", "play hand kc",
            "play hand 2c", "play hand 5h", "play hand 7c", "pickup", "play hand 4c", "play hand 4h",
            "play hand 8c", "play hand 8d", "play hand qs", "play hand 2h", "play hand kh",
            "play hand kc", "play hand ks", "play hand 2c", "play hand 2s", "play hand ah",
            "play hand 3h", "play hand 3d", "play hand 6c", "play hand 6s", "play hand jc",
            "play hand js", "play hand jd", "pickup", "play hand qh", "play hand qs", "play hand qd",
            "play hand 2h", "play hand xd", "play up 6h", "play hand 6d", "play up jh", "play hand kh",
            "pickup", "play hand ac", "play hand 6h", "play hand 7h", "play hand jh", "play hand kc",
            "play hand kh", "play hand ks", "pickup", "play hand 4s", "play hand 6d", "play hand 8h",
            "play hand jh", "play hand 2c", "play hand ac", "play hand 5s", "play hand 6h",
            "play hand 8s", "play hand kc", "play hand 2s", "play hand 7h", "play hand 7s",
            "play hand kh", "pickup", "play hand ks", "play hand 2c", "play up 3c", "play hand 5c",
            "play down 2", "play hand 9s", "play down 1", "play hand 5h", "play hand ks",
            "play hand kc", "play hand 2c", "play hand 7c", "play hand 2d", "play hand 4c",
            "play hand 5c", "play hand 8c", "play hand 9s", "play hand jc", "pickup", "play hand 4h",
            "play hand 5h", "play hand 8d", "play hand ks", "play hand 2s", "play hand ad",
            "play hand ah", "play hand 3c", "play hand 3h", "play hand kc", "play hand kh",
            "play hand 2c", "play hand 3d", "play hand 7c", "play hand js", "play hand 2d",
            "play hand 6c", "play hand 8c", "play hand jd", "play hand jc", "play hand jh",
            "pickup", "play hand 6s", "play hand 9s", "pickup", "play hand 4c", "play hand 4s",
            "play hand 5c", "play hand 6d", "play hand 8d", "play hand 8h", "play hand ks",
            "pickup", "play hand 4h", "play hand 5s", "play hand 5h", "play hand 6h", "play hand 2s",
            "play hand ac", "play hand ad", "play hand 8s", "play hand kc", "play hand ks",
            "play hand kh", "pickup", "play hand ah", "play hand 7h", "play hand 2c", "play hand 7s",
            "play hand 7c", "play hand 9s", "play hand js", "play hand 2s", "play hand 3c",
            "play hand 6s", "play hand 2d", "play hand 4c", "play hand 6c", "play hand 6d",
            "play hand 8c", "play hand 8d", "play hand jd", "play hand kc", "pickup", "play hand 4s",
            "play hand jc", "play hand ks", "play hand 2c", "play hand 5c", "play hand jh",
            "play hand kh", "play hand 2s", "play hand 8h", "play hand 9s", "pickup", "play hand 3h",
            "play hand 4h", "play hand 7h", "play hand 8s", "play hand js", "play hand jc",
            "play hand 2d", "play hand 5s", "play hand 7s", "play hand ks", "play hand kc",
            "play hand 2c", "play hand 3d", "play hand 5h", "play hand 7c", "play hand jh",
            "play hand jd", "play hand kh", "pickup", "play hand 6h", "play hand 6s", "play hand 2s",
            "play hand ah", "play hand ac", "play hand 3c", "play hand 4s", "play hand 4c",
            "play hand 5c", "play hand 6c", "play hand 8h", "play hand 8c", "play hand 9s",
            "play hand js", "pickup", "play hand 6d", "play hand 6h", "play hand 8d", "play hand 2s",
            "play hand 3h", "play hand 6s", "play hand 7h", "play hand 8h", "play hand 8s",
            "play hand 8c", "play hand jc", "play hand js", "play hand 2d", "play hand ad",
            "play hand 4h", "play hand 4s", "play hand 5s", "play hand 5c", "play hand 7s",
            "play hand 9s", "play hand ks", "pickup", "play hand kc", "play hand 2s", "play hand 2c",
            "play hand ah", "play hand 3d", "play hand 3c", "play hand 5h", "play hand 6c",
            "play hand 7c", "play hand 8d", "play hand jh", "play hand jc", "play hand jd",
            "play hand js", "play hand ac", "play hand kh", "play hand 2d", "play down 0",
            "play down 0", "play up 3s", "play up xh", "play down 0", "play down 0", "play up kd"
        };

        for (String command : commands) {
            if (game.isFinished()) {
                break;
            }
            parser.parse(game, command);
        }

        assertTrue(game.isFinished(), "Game should be finished after the hardcoded sequence of moves.");
        assertEquals(1, game.getPlayers().size(), "There should be only one player left (the winner).");
    }
}