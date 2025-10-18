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
    private static final VerificationWithTimeout TIMEOUT = timeout(1000);

    private static Server server;
    private static Connection connection;
    private static LobbyClient mockClient;

    @BeforeAll
    public static void setupServer() throws Exception {

        Database db = new MemoryDatabase();

        GameTypeRoom shithead = new GameTypeRoom();
        shithead.setName(GAME_TYPE_NAME);
        shithead.setServerClass(ShitHeadServer.class.getName());
        shithead.setClientClass("not needed");

        db.startTransaction();
        db.saveGameType(shithead);
        db.endTransaction();

        server = new Server(0, db);
        server.start();

        connection = new LobbyCom("test-normal-uuid", "junit-test", "1.0");
        mockClient = Mockito.mock(LobbyClient.class);
        connection.addEventListener(mockClient);

        connection.connect("localhost", server.getPort());

        // wait to be connected
        verify(mockClient, TIMEOUT).connected();

        verify(mockClient, TIMEOUT).setUsername("test-normal", Player.PLAYER_NORMAL);
    }

    @AfterAll
    public static void stopServer() {
        connection.disconnect();
        connection = null;

        server.shutdown();
        server = null;
    }

    @Test
    public void doTesting() {

        connection.getGameTypes();
        GameType shithead = getGameTypeFromServer(GAME_TYPE_NAME);
        connection.getGames(shithead);

        Game newGame = new Game("test game", null, 3, 100); // timeout is in seconds
        newGame.setType(shithead);
        connection.createNewGame(newGame);

        Game game = getGameFromServer();
        System.out.println("Game: " + game);
/*
        // TODO cant play it yet, need to have another client join
        connection.playGame(game.getId());

        ArgumentCaptor<Object> gameDataCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).messageForGame(eq(game.getId()), gameDataCaptor.capture());
        Object gameData = gameDataCaptor.getValue();
        System.out.println("Game Data: " + gameData);
 */
    }

    private static GameType getGameTypeFromServer(String name) {
        ArgumentCaptor<List<GameType>> gameTypeCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).addGameType(gameTypeCaptor.capture());
        List<GameType> gameTypes = gameTypeCaptor.getValue();
        System.out.println("Game Types: " + gameTypes);
        return gameTypes.stream().filter(gt -> name.equals(gt.getName())).findFirst().orElseThrow();
    }

    private static Game getGameFromServer() {
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.captor();
        verify(mockClient, TIMEOUT).addOrUpdateGame(gameCaptor.capture());
        return gameCaptor.getValue();
    }
}
