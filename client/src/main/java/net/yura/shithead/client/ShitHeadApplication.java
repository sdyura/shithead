package net.yura.shithead.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import net.yura.cardsengine.Card;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Window;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.shithead.common.AutoPlay;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;

public class ShitHeadApplication extends Application implements ActionListener {

    private Properties properties;
    private MiniLobbyClient minilobby;

    protected void initialize(DesktopPane dp) {

        setupTheme(dp);

        ResourceBundle bundle = ResourceBundle.getBundle("game_text");
        properties = new Properties() {
            @Override
            public String getProperty(String key) {
                return bundle.getString(key);
            }
        };

        openMainMenu();
    }

    protected void setupTheme(DesktopPane dp) {
        dp.setLookAndFeel(DesktopPane.getSystemLookAndFeelClassName());
    }

    private void openMainMenu() {
        System.out.println("OPENING MENU");

        XULLoader loader = new XULLoader();
        try (InputStream stream = ShitHeadApplication.class.getResourceAsStream("/main_menu.xml")) {
            loader.load(new InputStreamReader(stream), this, properties);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Frame frame = (Frame)loader.getRoot();
        frame.setMaximum(true);
        frame.setVisible(true);
    }

    public void actionPerformed(String actionCommand) {
        if ("play".equals(actionCommand)) {
            if (minilobby == null) {
                minilobby = new MiniLobbyClient(new MiniLobbyShithead(properties));
                minilobby.addCloseListener(this);
            }
            minilobby.connect("localhost");

            Frame frame = (Frame)DesktopPane.getDesktopPane().getSelectedFrame();
            frame.setContentPane(minilobby.getRoot());
            frame.revalidate();
            frame.repaint();
        }
        else if (Frame.CMD_CLOSE.equals(actionCommand)) { // close the lobby
            Window frame = minilobby.getRoot().getWindow();
            frame.setVisible(false);
            openMainMenu();
        }
        else if ("test".equals(actionCommand)) {

            ShitheadGame game = new ShitheadGame(4);
            game.deal();
            game.playerReady(game.getPlayers().get(0));
            Player me = game.getPlayers().get(1);
            game.playerReady(game.getPlayers().get(2));
            game.playerReady(game.getPlayers().get(3));

            final GameUI gameUI = new GameUI(properties, game, me.getName(), new ActionListener() {
                @Override
                public void actionPerformed(String actionCommand) {
                    CommandParser parser = new CommandParser();
                    parser.parse(game, actionCommand);

                    while (!game.isFinished() && game.isPlaying() && game.getCurrentPlayer() != me) {
                        List<Card> cards = AutoPlay.findBestVisibleCards(game);
                        if (cards.isEmpty()) {
                            parser.parse(game, "pickup");
                        }
                        else {
                            parser.parse(game, "play " + (game.getCurrentPlayer().getHand().contains(cards.get(0)) ? "hand " : "up ") +
                                    cards.stream().map(Object::toString).collect(Collectors.joining(" ")));
                        }
                    }

                    DesktopPane.getDesktopPane().getSelectedFrame().revalidate();
                    DesktopPane.getDesktopPane().getSelectedFrame().repaint();
                    //gameUI.newCommand(actionCommand);
                }
            });
        }
        else if ("about".equals(actionCommand)) {
            String versionName = System.getProperty("versionName");
            String versionCode = System.getProperty("versionCode");

            if (versionName == null) {
                try (InputStream stream = Application.getResourceAsStream("/META-INF/MANIFEST.MF")) {
                    Manifest manifest = new Manifest(stream);
                    Attributes attributes = manifest.getMainAttributes();
                    versionName = attributes.getValue("versionName");
                    versionCode = attributes.getValue("versionCode");
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            OptionPane.showMessageDialog(null, new String[] {"Version: " + versionName, "Build: " + versionCode}, properties.getProperty("about.title"), OptionPane.INFORMATION_MESSAGE);
        }
    }
}
