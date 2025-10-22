package net.yura.shithead.server;

import net.yura.lobby.client.Connection;
import net.yura.lobby.client.LobbyClient;
import net.yura.lobby.client.LobbyCom;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.lobby.model.Player;
import net.yura.lobby.netty.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationWithTimeout;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class ServerTest {

    private static final String PLAYER_1_NAME = "test-normal";
    private static final String PLAYER_2_NAME = "test-guest";

    private static final VerificationWithTimeout TIMEOUT = timeout(1000);

    private static Server server;
    private static Connection connection1;
    private static LobbyClient mockClient1;
    private static Connection connection2;
    private static LobbyClient mockClient2;

    @BeforeAll
    public static void setupServer() throws Exception {

        server = TestServer.startTestServer(0);

        connection1 = new LobbyCom(PLAYER_1_NAME + "-uuid", "junit-test", "1.0");
        mockClient1 = Mockito.mock(LobbyClient.class);
        connection1.addEventListener(mockClient1);
        connection1.connect("localhost", server.getPort());
        verify(mockClient1, TIMEOUT).connected();
        verify(mockClient1, TIMEOUT).setUsername(PLAYER_1_NAME, Player.PLAYER_NORMAL);

        connection2 = new LobbyCom(PLAYER_2_NAME + "-uuid", "junit-test", "1.0");
        mockClient2 = Mockito.mock(LobbyClient.class);
        connection2.addEventListener(mockClient2);
        connection2.connect("localhost", server.getPort());
        verify(mockClient2, TIMEOUT).connected();
        verify(mockClient2, TIMEOUT).setUsername(PLAYER_2_NAME, Player.PLAYER_GUEST);
    }

    @AfterAll
    public static void stopServer() {
        connection1.disconnect();
        connection1 = null;
        connection2.disconnect();
        connection2 = null;

        server.shutdown();
        server = null;
    }

    @Test
    public void doTesting() {

        // player 1 register for updates
        connection1.getGameTypes();
        GameType shithead = getGameTypeFromServer(mockClient1, TestServer.GAME_TYPE_NAME);
        connection1.getGames(shithead);

        // player 1 create new game
        Game newGame = new Game("test game", null, 2, 100);
        newGame.setType(shithead);
        connection1.createNewGame(newGame);

        Game game = getGameFromServer(mockClient1);
        System.out.println("Game: " + game);
        assertEquals(1, game.getNumOfPlayers());
        assertEquals(2, game.getMaxPlayers());

        // player 2 register for updates
        connection2.getGameTypes();
        connection2.getGames(getGameTypeFromServer(mockClient2, TestServer.GAME_TYPE_NAME));
        getGameFromServer(mockClient2);

        // player 2 joins the game
        connection2.joinGame(game.getId(), null);
        game = getGameFromServer(mockClient2); // this will actually get called twice, once for joining, once for game asking for input
        System.out.println("Game started: " + game +" " + game.getNumOfPlayers() + "/" + game.getMaxPlayers());
        assertEquals(2, game.getNumOfPlayers());
        assertEquals(2, game.getMaxPlayers());

        connection1.playGame(game.getId());
        Object gameObj1 = messageForGame(mockClient1, game.getId());
        System.out.println("Game obj1: " + gameObj1);
        assertNotNull(gameObj1);

        connection2.playGame(game.getId());
        Object gameObj2 = messageForGame(mockClient2, game.getId());
        System.out.println("Game obj2: " + gameObj2);
        assertNotNull(gameObj2);

        // player 1 renames
        String newName = "new name";
        connection1.setNick(newName);

        // get the rename command from both players
        Object rename1 = messageForGame(mockClient1, game.getId());
        Object rename2 = messageForGame(mockClient2, game.getId());

        assertEquals("rename test-normal new%20name", rename1);
        assertEquals("rename test-normal new%20name", rename2);
    }

    private static GameType getGameTypeFromServer(LobbyClient mockClient, String name) {
        ArgumentCaptor<List<GameType>> gameTypeCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).addGameType(gameTypeCaptor.capture());
        clearInvocations(mockClient);
        List<GameType> gameTypes = gameTypeCaptor.getValue();
        System.out.println("Game Types: " + gameTypes);
        return gameTypes.stream().filter(gt -> name.equals(gt.getName())).findFirst().orElseThrow();
    }

    private static Game getGameFromServer(LobbyClient mockClient) {
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT.atLeastOnce()).addOrUpdateGame(gameCaptor.capture());
        clearInvocations(mockClient);
        return gameCaptor.getValue();
    }

    private static Object messageForGame(LobbyClient mockClient, int id) {
        ArgumentCaptor<Object> gameCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).messageForGame(eq(id), gameCaptor.capture());
        clearInvocations(mockClient);
        return gameCaptor.getValue();
    }
}
