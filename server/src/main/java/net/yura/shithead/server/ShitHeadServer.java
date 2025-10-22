package net.yura.shithead.server;

import net.yura.lobby.server.AbstractTurnBasedServerGame;
import net.yura.lobby.server.LobbySession;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.common.json.SerializerUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        // TODO may need to shuffle the players
        game = new ShitheadGame(Arrays.asList(players));
        game.deal();
        getInputFromClient(game.getCurrentPlayer().getName());
    }

    @Override
    public void clientHasJoined(LobbySession lobbySession) {
        String username = lobbySession.getUsername();
        String json = SerializerUtil.toJSON(game, username);
        // TODO maybe we want to gzip
        listoner.messageFromGame(json, Collections.singletonList(lobbySession));
    }

    @Override
    public void destroyGame() { }

    @Override
    public void playerJoins(String s) { }

    @Override
    public boolean playerResigns(String s) {
        return false;
    }

    @Override
    public void midgamePlayerLogin(String oldName, String newName) {
        game.renamePlayer(oldName, newName);

        // after move, notify all players
        try {
            String renameCommand = "rename " + URLEncoder.encode(oldName, StandardCharsets.UTF_8.name()).replace("+", "%20") + " " + URLEncoder.encode(newName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            for (LobbySession session : getAllClients()) {
                listoner.messageFromGame(renameCommand, Collections.singletonList(session));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void playerTimedOut(String s) {

    }

    @Override
    public void objectFromPlayer(String username, Object o) {
        if (!username.equals(game.getCurrentPlayer().getName())) {
            throw new RuntimeException("not your turn");
        }
        String command = (String) o;
        commandParser.execute(game, command);

        // after move, notify all players
        for (LobbySession session : getAllClients()) {
            listoner.messageFromGame(command, Collections.singletonList(session));
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
