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
        // we are in re-arrange mode, anyone can go
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
        game.removePlayer(s);
        if (game.getPlayers().size() == 1) {
            return gameFinished(game.getPlayers().get(0).getName());
        }
        return false;
    }

    @Override
    public void midgamePlayerLogin(String oldName, String newName) {

        // TODO is this really needed, can we just use player events in new lobby version
        //game.renamePlayer(oldName, newName);

        String renameCommand = "rename " + CommandParser.encodePlayerName(oldName) + " " + CommandParser.encodePlayerName(newName);

        commandParser.execute(game, renameCommand);
        listoner.messageFromGame(renameCommand, getAllClients());
    }

    @Override
    public void playerTimedOut(String username) {

    }

    @Override
    public void objectFromPlayer(String username, Object o) {
        if (game.isPlaying() && !username.equals(game.getCurrentPlayer().getName())) {
            throw new RuntimeException("not your turn");
        }
        String command = (String) o;
        commandParser.execute(game, command);

        // after move, notify all players
        listoner.messageFromGame(command, getAllClients());
        if (game.isPlaying()) {
            getInputFromClient(game.getCurrentPlayer().getName());
        }
    }

    @Override
    public boolean isSupportedClient(LobbySession lobbySession) {
        return true;
    }

    @Override
    public void loadGame(byte[] bytes) {
        game = SerializerUtil.fromJSON(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public byte[] saveGameState() {
        String json = SerializerUtil.toJSON(game, null);
        return json.getBytes(StandardCharsets.UTF_8);
    }

}
