package net.yura.shithead.server;

import net.yura.lobby.server.AbstractTurnBasedServerGame;
import net.yura.lobby.server.LobbySession;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.common.json.SerializerUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ShitHeadServer extends AbstractTurnBasedServerGame {

    private static Logger logger = Logger.getLogger(ShitHeadServer.class.getName());

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
        // TODO do we want to do autoplay and then resign player??
    }

    @Override
    public void objectFromPlayer(String username, Object o) {
        String command = (String) o;

        if (game.isRearranging() && !username.equals(CommandParser.decodePlayerName(command.split(" ")[1]))) {
            throw new RuntimeException("not your player");
        }
        if (game.isPlaying() && !username.equals(game.getCurrentPlayer().getName())) {
            logger.warning("not your turn error! currentPlayer=" + game.getCurrentPlayer().getName() +" fromPlayer=" + username +" command=" + command);
            throw new RuntimeException("not your turn");
        }

        String mutation = CommandParser.getMutationCommand(game, command);
        commandParser.execute(game, mutation);

        // after move, notify all players
        Collection<LobbySession> allClients = getAllClients();
        if (mutation.startsWith("play hand ") && mutation.contains("pickup")) {
            // for picking up a card we ONLY want to tell the actual player what the card is
            listoner.messageFromGame(mutation, allClients.stream().filter(c -> username.equals(c.getUsername())).collect(Collectors.toList()));
            listoner.messageFromGame(command, allClients.stream().filter(c -> !username.equals(c.getUsername())).collect(Collectors.toList()));
        }
        else {
            listoner.messageFromGame(mutation, allClients);
        }

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
