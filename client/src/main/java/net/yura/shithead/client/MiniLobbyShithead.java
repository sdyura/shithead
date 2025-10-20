package net.yura.shithead.client;

import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.lobby.mini.MiniLobbyGame;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.lobby.model.Player;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.util.Properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.jar.Manifest;

public class MiniLobbyShithead implements MiniLobbyGame {

    Properties strings;

    protected MiniLobbyClient lobby;

    public MiniLobbyShithead(Properties strings) {
        this.strings = strings;
    }

    @Override
    public void addLobbyGameMoveListener(MiniLobbyClient lgl) {
        lobby = lgl;
    }

    @Override
    public Properties getProperties() {
        return strings;
    }

    @Override
    public void openChat() {
        // TODO
    }

    @Override
    public boolean isMyGameType(GameType gametype) {
        return "Shithead".equals(gametype.getName());
    }

    @Override
    public Icon getIconForGame(Game game) {
        return null;
    }

    @Override
    public String getGameDescription(Game game) {
        return "";
    }

    @Override
    public void openGameSetup(GameType gameType) {
        // TODO game setup
    }

    @Override
    public void prepareAndOpenGame(Game game) {
        lobby.mycom.playGame(game.getId());
    }

    @Override
    public void objectForGame(Object object) {

    }

    @Override
    public void stringForGame(String message) {

    }

    @Override
    public void connected(String username) {
        MiniLobbyClient.toast("Logged in as: " + username);
    }

    @Override
    public void disconnected() {

    }

    @Override
    public void loginGoogle() {

    }

    @Override
    public void gameStarted(int id) {

    }

    @Override
    public String getAppName() {
        return "Shithead";
    }

    @Override
    public String getAppVersion() {
        String versionName = System.getProperty("versionName");
        if (versionName == null) {
            try (InputStream stream = Application.getResourceAsStream("/META-INF/MANIFEST.MF")) {
                versionName = new Manifest(stream).getMainAttributes().getValue("versionName");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return versionName;
    }

    @Override
    public void lobbyShutdown() {

    }

    @Override
    public void showMessage(String fromwho, String message) {
        MiniLobbyClient.toast(fromwho != null ? fromwho + ": " + message : message);
    }

    @Override
    public void addSpectator(Player player) { }
    @Override
    public void removeSpectator(String player) { }
    @Override
    public void renameSpectator(String oldname, String newname, int newtype) { }
    @Override
    public void updatePlayerList(Collection<Player> playersInGame, String whoTurn) { }
    @Override
    public void gameActionPerformed(int state) { }
}
