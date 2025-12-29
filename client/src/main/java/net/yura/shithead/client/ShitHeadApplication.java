package net.yura.shithead.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Spinner;
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
        if ("multiplayer".equals(actionCommand)) {
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
        else if ("singleplayer".equals(actionCommand)) {

            XULLoader gameSetupLoader = createNewGameScreen(properties, loader -> {
                Spinner players = (Spinner)loader.find("players");
                int numPlayers = (Integer)players.getValue();
                createNewGame(numPlayers);
            });

            gameSetupLoader.find("gamename").setVisible(false);
            gameSetupLoader.find("gamenameLabel").setVisible(false);

            Frame dialog = (Frame)gameSetupLoader.getRoot();
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
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
        else if (Frame.CMD_CLOSE.equals(actionCommand)) { // close the lobby
            Window frame = minilobby.getRoot().getWindow();
            frame.setVisible(false);
            openMainMenu();
        }
        else {
            System.out.println("unknown command: " + actionCommand);
        }
    }

    private void createNewGame(int numPlayers) {

        ShitheadGame game = new ShitheadGame(numPlayers);
        int myIndex = new Random().nextInt(numPlayers);
        Player me = game.getPlayers().get(myIndex);
        game.deal();

        for (Player player : game.getPlayers()) {
            if (me != player) {
                game.playerReady(player);
            }
        }

        final GameUI gameUI = new GameUI(properties, game, me.getName(), new ActionListener() {
            @Override
            public void actionPerformed(String actionCommand) {
                CommandParser parser = new CommandParser();
                parser.parse(game, actionCommand);

                // TODO ideally we would call GameView.layoutCards() directly as when we use revalidate it may or not get called

                DesktopPane.getDesktopPane().getSelectedFrame().revalidate();
                DesktopPane.getDesktopPane().getSelectedFrame().repaint();

                new Thread() {
                    @Override
                    public void run() {
                        while (!game.isFinished() && game.isPlaying() && game.getCurrentPlayer() != me) {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                break;
                            }
                            parser.parse(game, AutoPlay.getValidGameCommand(game));
                            DesktopPane.getDesktopPane().getSelectedFrame().revalidate();
                            DesktopPane.getDesktopPane().getSelectedFrame().repaint();
                        }
                    }
                }.start();
            }
        });
    }


    public static XULLoader createNewGameScreen(Properties properties, Consumer<XULLoader> actionListener) {
        final XULLoader gameSetupLoader = new XULLoader();
        try (InputStreamReader reader = new InputStreamReader(ShitHeadApplication.class.getResourceAsStream("/game_setup.xml"))) {
            gameSetupLoader.load(reader, actionCommand -> {
                if ("create".equals(actionCommand)) {
                    actionListener.accept(gameSetupLoader);
                }
                ((Frame)gameSetupLoader.getRoot()).setVisible(false);
            }, properties);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return gameSetupLoader;
    }
}
