package net.yura.shithead.server;

import net.yura.lobby.server.AbstractTurnBasedServerGame;
import net.yura.lobby.server.LobbySession;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.common.json.SerializerUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

public class ShitHeadServer extends AbstractTurnBasedServerGame {

    ShitheadGame game;
    private final CommandParser commandParser;

    public ShitHeadServer() {
        this.commandParser = new CommandParser();
    }

    @Override
    public void startGame(String[] players) {
        game = new ShitheadGame(Arrays.asList(players));
        game.deal();
    }

    @Override
    public void clientHasJoined(LobbySession lobbySession) {
        String username = lobbySession.getUsername();
        String json = SerializerUtil.toJSON(game, username);
        // TODO maybe we want to gzip
        listoner.messageFromGame(json, Collections.singletonList(lobbySession));
    }

    @Override
    public void destroyGame() {

    }

    @Override
    public void playerJoins(String s) {

    }

    @Override
    public boolean playerResigns(String s) {
        return false;
    }

    @Override
    public void midgamePlayerLogin(String s, String s1) {

    }

    @Override
    public void playerTimedOut(String s) {

    }

    @Override
    public void objectFromPlayer(String username, Object o) {
        if (!username.equals(game.getCurrentPlayer().getName())) {
            throw new RuntimeException("not your turn");
        }
        commandParser.execute(game, (String) o);

        // after move, notify all players
        for (LobbySession session : listSessions()) {
            String json = SerializerUtil.toJSON(game, session.getUsername());
            listoner.messageFromGame(json, Collections.singletonList(session));
        }
    }

    @Override
    public boolean isSupportedClient(LobbySession lobbySession) {
        return true;
    }

    @Override
    public void loadGame(byte[] bytes) {
        // TODO maybe we want to gzip
        game = SerializerUtil.fromJSON(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public byte[] saveGameState() {
        String json = SerializerUtil.toJSON(game, null);
        // TODO maybe we want to gzip
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
