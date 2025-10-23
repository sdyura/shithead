package net.yura.lobby.server;

public interface ServerGameListener {
    void sendChatroomMessage(String message);
    boolean gameFinished(String winner);
    void gameStarted();
    void needInputFrom(String username);
    void messageFromGame(Object object, java.util.List<net.yura.lobby.server.LobbySession> lobbySessions);
}
