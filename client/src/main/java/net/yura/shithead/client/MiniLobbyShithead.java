package net.yura.shithead.client;

import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.lobby.mini.MiniLobbyGame;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.lobby.model.Player;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.util.Properties;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Spinner;
import net.yura.mobile.gui.components.TextField;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.layout.XULLoader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.jar.Manifest;

public class MiniLobbyShithead implements MiniLobbyGame, ActionListener {

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

    private GameType gameType;
    private XULLoader gameSetupLoader;

    @Override
    public void openGameSetup(GameType gameType) {
        this.gameType = gameType;
        gameSetupLoader = new XULLoader();
        try (InputStreamReader reader = new InputStreamReader(ShitHeadClient.class.getResourceAsStream("/game_setup.xml"))) {
            gameSetupLoader.load(reader, this, strings);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        ((Frame)gameSetupLoader.getRoot()).setVisible(true);
    }

    @Override
    public void actionPerformed(String actionCommand) {
        if ("create".equals(actionCommand)) {
            Spinner players = (Spinner)gameSetupLoader.find("players");
            int numPlayers = (Integer)players.getValue();
            TextField gamename = (TextField)gameSetupLoader.find("gamename");
            String gameName = gamename.getText();
            if (gameName.isEmpty()) {
                gameName = lobby.whoAmI() + "'s game";
            }
            Game newGame = new Game(gameName, null, numPlayers, 100);
            newGame.setType(gameType);
            lobby.createNewGame(newGame);
            ((Frame)gameSetupLoader.getRoot()).setVisible(false);
            gameSetupLoader = null;
            gameType = null;
        }
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
