package net.yura.shithead.server;

import net.yura.shithead.common.ShitheadGame;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShitHeadServerTest {

    @Test
    public void testCurrentPlayerAfterResignation() {
        ShitheadGame game = new ShitheadGame(Arrays.asList("player1", "player2", "player3"));
        game.setCurrentPlayer(1); // player2's turn

        game.removePlayer("player1"); // player before current player resigns

        assertEquals("player2", game.getCurrentPlayer().getName());
    }

    @Test
    public void testCurrentPlayerResigns() {
        ShitheadGame game = new ShitheadGame(Arrays.asList("player1", "player2", "player3"));
        game.setCurrentPlayer(1); // player2's turn

        game.removePlayer("player2"); // current player resigns

        assertEquals("player3", game.getCurrentPlayer().getName());
    }
}
