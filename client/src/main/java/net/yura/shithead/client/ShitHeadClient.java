package net.yura.shithead.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.Window;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.uicomponents.GameView;

public class ShitHeadClient extends Application implements ActionListener {

    private Properties properties;
    private GameView gameView;

    private MiniLobbyClient minilobby;

    protected void initialize(DesktopPane dp) {

        dp.setLookAndFeel(DesktopPane.getSystemLookAndFeelClassName());

        ResourceBundle bundle = ResourceBundle.getBundle("game_text");
        properties = new Properties() {
            @Override
            public String getProperty(String key) {
                return bundle.getString(key);
            }
        };

        openMainMenu();
    }

    private void openMainMenu() {
        XULLoader loader = new XULLoader();
        try (InputStream stream = ShitHeadClient.class.getResourceAsStream("/main_menu.xml")) {
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
            try {
                XULLoader loader = new XULLoader();
                try (InputStream stream = ShitHeadClient.class.getResourceAsStream("/game_view.xml")) {
                    loader.load(new InputStreamReader(stream), this, properties);
                }

                gameView = (GameView)loader.find("game_view");
                ShitheadGame game = new ShitheadGame(4);
                game.deal();
                gameView.setGame(game);
                gameView.setPlayerID("Player 1");

                Frame frame = (Frame)DesktopPane.getDesktopPane().getSelectedFrame();
                frame.setContentPane( (Panel)loader.getRoot() );
                frame.revalidate();
                frame.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
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