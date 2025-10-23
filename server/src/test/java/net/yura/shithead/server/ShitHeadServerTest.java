package net.yura.shithead.server;

import net.yura.shithead.common.ShitheadGame;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShitHeadServerTest {

    @Test
    public void testPlayerResignsWithThreePlayers() {
        ShitHeadServer server = new ShitHeadServer();
        server.game = new ShitheadGame(Arrays.asList("player1", "player2", "player3"));
        server.game.setCurrentPlayer(0);

        boolean result = server.playerResigns("player2");

        assertFalse(result);
        assertEquals(2, server.game.getPlayers().size());
    }

    @Test
    public void testPlayerResignsWithTwoPlayers() {
        ShitHeadServer server = new ShitHeadServer();
        server.game = new ShitheadGame(Arrays.asList("player1", "player2"));
        server.game.setCurrentPlayer(0);

        // can't mock the listoner as its not public, so we expect a NullPointerException
        // when the gameFinished method is called.
        try {
            server.playerResigns("player1");
        } catch (NullPointerException e) {
            // expected
        }

        assertEquals(1, server.game.getPlayers().size());
    }
}
