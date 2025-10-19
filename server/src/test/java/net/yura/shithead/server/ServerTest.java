package net.yura.shithead.server;

import net.yura.lobby.client.Connection;
import net.yura.lobby.client.LobbyClient;
import net.yura.lobby.client.LobbyCom;
import net.yura.lobby.database.Database;
import net.yura.lobby.database.GameTypeRoom;
import net.yura.lobby.database.impl.MemoryDatabase;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class ServerTest {

    private static final String GAME_TYPE_NAME = "Shithead";
    private static final String PLAYER_1_NAME = "player1";
    private static final String PLAYER_2_NAME = "player2";
    private static final VerificationWithTimeout TIMEOUT = timeout(1000);

    private static Server server;
    private static Connection connection1;
    private static LobbyClient mockClient1;
    private static Connection connection2;
    private static LobbyClient mockClient2;

    @BeforeAll
    public static void setupServer() throws Exception {

        Database db = new MemoryDatabase();

        GameTypeRoom shithead = new GameTypeRoom();
        shithead.setName(GAME_TYPE_NAME);
        shithead.setServerClass(ShitHeadServer.class.getName());
        shithead.setClientClass("not needed");
        shithead.setMinPlayers(2);
        shithead.setMaxPlayers(2);

        db.startTransaction();
        db.saveGameType(shithead);
        db.endTransaction();

        server = new Server(0, db);
        server.start();

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
        verify(mockClient2, TIMEOUT).setUsername(PLAYER_2_NAME, Player.PLAYER_NORMAL);
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

        connection1.getGameTypes();
        GameType shithead = getGameTypeFromServer(mockClient1, GAME_TYPE_NAME);
        connection1.getGames(shithead);

        Game newGame = new Game("test game", null, 2, 100);
        newGame.setType(shithead);
        connection1.createNewGame(newGame);

        Game game = getGameFromServer(mockClient1);
        System.out.println("Game: " + game);

        // player 2 joins the game
        connection2.joinGame(game.getId());

        // TODO need to check that both players get a notification that the game has started
    }

    private static GameType getGameTypeFromServer(LobbyClient mockClient, String name) {
        ArgumentCaptor<List<GameType>> gameTypeCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).addGameType(gameTypeCaptor.capture());
        List<GameType> gameTypes = gameTypeCaptor.getValue();
        System.out.println("Game Types: " + gameTypes);
        return gameTypes.stream().filter(gt -> name.equals(gt.getName())).findFirst().orElseThrow();
    }

    private static Game getGameFromServer(LobbyClient mockClient) {
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).addOrUpdateGame(gameCaptor.capture());
        return gameCaptor.getValue();
    }
}
