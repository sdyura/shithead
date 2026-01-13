package net.yura.shithead.server;

import net.yura.lobby.client.Connection;
import net.yura.lobby.client.LobbyClient;
import net.yura.lobby.client.LobbyCom;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.lobby.model.Player;
import net.yura.lobby.netty.Server;
import net.yura.shithead.common.AutoPlay;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.common.json.SerializerUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

    static class MockClient implements AutoCloseable {
        private String username;
        final Connection connection;
        final LobbyClient clientMock;
        ShitheadGame game;

        MockClient(String username, int type) {
            this.username = username;
            connection = new LobbyCom(username + "-uuid", "junit-test", "1.0");
            clientMock = Mockito.mock(LobbyClient.class);
            connection.addEventListener(clientMock);
            connection.connect("localhost", server.getPort());
            verify(clientMock, TIMEOUT).connected();
            verify(clientMock, TIMEOUT).setUsername(username, type);
        }

        public void close() {
            connection.disconnect();
        }

        public void setGame(Object gameObj) {
            System.out.println("Game for: " + username + " " + gameObj);
            assertNotNull(gameObj);
            game = SerializerUtil.fromJSON((String) gameObj);
        }

        public void mutateGame(Object mutation) {
            new CommandParser().execute(game, (String) mutation);
        }
    }

    private static MockClient mockClient1;
    private static MockClient mockClient2;

    // TODO when updated to next server version, can change this back to BeforeAll/AfterAll
    // currently the rename player test messes up other tests as its not possible to reset
    //@BeforeAll
    @BeforeEach
    public void setupServer() throws Exception {

        server = TestServer.startTestServer(0);

        mockClient1 = new MockClient(PLAYER_1_NAME, Player.PLAYER_NORMAL);
        mockClient2 = new MockClient(PLAYER_2_NAME, Player.PLAYER_GUEST);
    }

    //@AfterAll
    @AfterEach
    public void stopServer() {
        mockClient1.close();
        mockClient1 = null;
        mockClient2.close();
        mockClient2 = null;

        server.shutdown();
        server = null;
    }

    public int bothPlayersJoinGame() {

        // player 1 register for updates
        mockClient1.connection.getGameTypes();
        GameType shithead = getGameTypeFromServer(mockClient1.clientMock, TestServer.GAME_TYPE_NAME);
        mockClient1.connection.getGames(shithead);

        // player 1 create new game
        Game newGame = new Game("test game", null, 2, 100);
        newGame.setType(shithead);
        mockClient1.connection.createNewGame(newGame);

        Game game = getGameFromServer(mockClient1.clientMock);
        System.out.println("Game: " + game);
        assertEquals(1, game.getNumOfPlayers());
        assertEquals(2, game.getMaxPlayers());

        // player 2 register for updates
        mockClient2.connection.getGameTypes();
        mockClient2.connection.getGames(getGameTypeFromServer(mockClient2.clientMock, TestServer.GAME_TYPE_NAME));
        getGameFromServer(mockClient2.clientMock);

        // player 2 joins the game
        mockClient2.connection.joinGame(game.getId(), null);
        game = getGameFromServer(mockClient2.clientMock); // this will actually get called twice, once for joining, once for game asking for input
        System.out.println("Game started: " + game +" " + game.getNumOfPlayers() + "/" + game.getMaxPlayers());
        assertEquals(2, game.getNumOfPlayers());
        assertEquals(2, game.getMaxPlayers());

        mockClient1.connection.playGame(game.getId());
        Object gameObj1 = messageForGame(mockClient1.clientMock, game.getId());
        mockClient1.setGame(gameObj1);

        mockClient2.connection.playGame(game.getId());
        Object gameObj2 = messageForGame(mockClient2.clientMock, game.getId());
        mockClient2.setGame(gameObj2);

        return game.getId();
    }

    @Test
    public void test2PlayersJoinGame() {
        int id = bothPlayersJoinGame();

        sendGameMessage(mockClient1, id, "ready " + PLAYER_1_NAME);
        sendGameMessage(mockClient2, id, "ready " + PLAYER_2_NAME);

        while (!mockClient1.game.isFinished() || !mockClient2.game.isFinished()) {

            String whosTurn = mockClient1.game.getCurrentPlayer().getName();
            if (mockClient1.username.equals(whosTurn)) {
                sendGameMessage(mockClient1, id, AutoPlay.getValidGameCommand(mockClient1.game));
            }
            else if (mockClient2.username.equals(whosTurn)) {
                sendGameMessage(mockClient2, id, AutoPlay.getValidGameCommand(mockClient2.game));
            }
            else {
                throw new IllegalStateException("whos turn??? " + whosTurn);
            }
        }
    }

    private void sendGameMessage(MockClient mockClient, int id, String command) {
        mockClient.connection.sendGameMessage(id, command);
        Object mutation1 = messageForGame(mockClient1.clientMock, id);
        mockClient1.mutateGame(mutation1);
        Object mutation2 = messageForGame(mockClient2.clientMock, id);
        mockClient2.mutateGame(mutation2);
    }

    @Test
    public void testMidGameLogin() {
        int gameId = bothPlayersJoinGame();

        // player 1 renames
        String newName = "new name";
        mockClient1.connection.setNick(newName);

        // get the rename command from both players
        Object rename1 = messageForGame(mockClient1.clientMock, gameId);
        Object rename2 = messageForGame(mockClient2.clientMock, gameId);

        assertEquals("rename " + PLAYER_1_NAME + " new+name", rename1);
        assertEquals("rename " + PLAYER_1_NAME + " new+name", rename2);

        // now reset it
        // TODO broken on current version of lobby server, fixed in next version
        //server.getLobbyController().lobby.setNick(newName, "test-normal");
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
