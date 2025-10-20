package net.yura.shithead.server;

import net.yura.lobby.database.Database;
import net.yura.lobby.database.GameTypeRoom;
import net.yura.lobby.database.impl.MemoryDatabase;
import net.yura.lobby.netty.Server;

public class TestServer {

    static final String GAME_TYPE_NAME = "Shithead";

    public static void main(String[] args) {
        System.out.println("starting test server...");
        startTestServer(1964);
    }

    public static Server startTestServer(int port) {
        Database db = new MemoryDatabase();

        GameTypeRoom shithead = new GameTypeRoom();
        shithead.setName(GAME_TYPE_NAME);
        shithead.setServerClass(ShitHeadServer.class.getName());
        shithead.setClientClass("not needed");

        db.startTransaction();
        db.saveGameType(shithead);
        db.endTransaction();

        Server server = new Server(port, db);
        try {
            server.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return server;
    }
}
