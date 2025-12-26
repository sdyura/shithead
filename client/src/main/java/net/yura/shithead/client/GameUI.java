package net.yura.shithead.client;

import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.MenuBar;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.uicomponents.GameView;

import java.io.InputStream;
import java.io.InputStreamReader;

public class GameUI implements ActionListener {

    ShitheadGame game;
    GameView gameView;
    ActionListener gameCommandActionListener;
    ActionListener closeActionListener;

    public GameUI(Properties properties, ShitheadGame game, ActionListener gameCommandActionListener) {
        this.game = game;
        this.gameCommandActionListener = gameCommandActionListener;

        XULLoader loader = new XULLoader();
        try (InputStream stream = ShitHeadApplication.class.getResourceAsStream("/game_view.xml")) {
            loader.load(new InputStreamReader(stream), this, properties);
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        gameView = (GameView) loader.find("game_view");
        gameView.setGame(game);
        gameView.setPlayerID("Player 1");

        Frame frame = (Frame)loader.getRoot();

        MenuBar menuBar = (MenuBar) loader.find("menu_bar");

        Button backButton = new Button(properties.getProperty("game.back"));
        backButton.setActionCommand(Frame.CMD_CLOSE);
        backButton.addActionListener(this);
        menuBar.add(backButton);



        frame.setMaximum(true);
        frame.revalidate();
        frame.setVisible(true);
    }


    @Override
    public void actionPerformed(String actionCommand) {
        if (Frame.CMD_CLOSE.equals(actionCommand)) {
            if (closeActionListener != null) {
                closeActionListener.actionPerformed(actionCommand);
            }
            close();
        }
        // TODO game actions!!
    }

    public void close() {
        gameView.getWindow().setVisible(false);
    }

    public void newCommand(String message) {
        new CommandParser().execute(game, message);
        gameView.repaint();
    }
}
