package net.yura.shithead.server;

import net.yura.lobby.server.ServerGameListener;
import net.yura.shithead.common.ShitheadGame;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShitHeadServerTest {

    @Test
    public void testPlayerResignsWithThreePlayers() {
        ShitHeadServer server = new ShitHeadServer();
        server.addServerGameListener(mock(ServerGameListener.class));

        server.game = new ShitheadGame(Arrays.asList("player1", "player2", "player3"));
        server.game.setCurrentPlayer(0);

        boolean result = server.playerResigns("player2");

        assertFalse(result);
        //assertEquals(2, server.game.getPlayers().size());
    }

    @Test
    public void testPlayerResignsWithTwoPlayers() {
        ShitHeadServer server = new ShitHeadServer();
        server.game = new ShitheadGame(Arrays.asList("player1", "player2"));
        server.game.setCurrentPlayer(0);

        ServerGameListener listener = mock(ServerGameListener.class);
        when(listener.gameFinished(any())).thenReturn(true);
        server.addServerGameListener(listener);

        boolean result1 = server.playerResigns("player1");
        assertFalse(result1);

        boolean result2 = server.playerResigns("player2");
        assertTrue(result2);
        //assertEquals(1, server.game.getPlayers().size());
    }

    @Test
    public void testCurrentPlayerIsNullBeforeGameStarts() {
        ShitHeadServer server = new ShitHeadServer();
        server.addServerGameListener(mock(ServerGameListener.class));

        server.game = new ShitheadGame(Arrays.asList("player1", "player2", "player3"));

        assertNull(server.game.getCurrentPlayer());
    }
}
