package net.yura.shithead.client;

import net.yura.cardsengine.Card;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.MenuBar;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.shithead.common.CardComparator;
import net.yura.shithead.common.CommandParser;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import net.yura.shithead.uicomponents.CardLocation;
import net.yura.shithead.uicomponents.GameView;
import net.yura.shithead.uicomponents.GameViewListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameUI implements ActionListener, GameViewListener {

    final ShitheadGame game;
    final GameView gameView;
    final ActionListener gameCommandListener;
    ActionListener closeActionListener;
    private final String playerUsername;

    public GameUI(Properties properties, ShitheadGame game, String playerUsername, ActionListener gameCommandActionListener) {
        this.game = game;
        this.playerUsername = playerUsername;
        this.gameCommandListener = gameCommandActionListener;

        XULLoader loader = new XULLoader();
        try (InputStream stream = ShitHeadApplication.class.getResourceAsStream("/game_view.xml")) {
            loader.load(new InputStreamReader(stream), this, properties);
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        gameView = (GameView) loader.find("game_view");
        gameView.setGameCommandListener(this);
        gameView.setGame(game, playerUsername);

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
    public void swapCards(Card card1, Card card2) {
        gameCommandListener.actionPerformed("swap " + CommandParser.encodePlayerName(playerUsername) +" " + card1 + " " + card2);
    }

    @Override
    public void playVisibleCard(boolean hand, Card card) {
        gameCommandListener.actionPerformed("play " + (hand ? "hand " : "up ") + card);
    }

    @Override
    public void pickUpWaste() {
        gameCommandListener.actionPerformed("pickup");
    }

    @Override
    public void playDeck() {
        gameCommandListener.actionPerformed("play deck");
    }

    @Override
    public void playDowncard() {
        gameCommandListener.actionPerformed("play down 0");
    }

    @Override
    public void actionPerformed(String actionCommand) {
        if (Frame.CMD_CLOSE.equals(actionCommand)) {
            if (closeActionListener != null) {
                closeActionListener.actionPerformed(actionCommand);
            }
            close();
        }
        else if ("play".equals(actionCommand)) {
            // TODO for now, only ready command
            gameCommandListener.actionPerformed("ready " + CommandParser.encodePlayerName(playerUsername));
        }
        else if ("sort".equals(actionCommand)) {
            Player player = getPlayer(playerUsername);
            if (player != null) {
                sortHand(player);
            }
            gameView.revalidate();
            gameView.repaint();
        }
        else {
            // TODO game actions!!
            System.out.println("unknown command " + actionCommand);
        }
    }

    public void close() {
        gameView.getWindow().setVisible(false);
    }

    public void newCommand(String message) {
        new CommandParser().execute(game, message);
        gameView.repaint();
    }

    public static void sortHand(Player player) {
        if (player != null) {
            List<Card> hand = player.getHand();
            List<Card> sortedHand = new ArrayList<>(hand);
            sortedHand.sort(new CardComparator());
            if (hand.equals(sortedHand)) {
                Collections.reverse(hand);
            } else {
                hand.clear();
                hand.addAll(sortedHand);
            }
        }
    }

    private Player getPlayer(String playerName) {
        for (Player player : game.getPlayers()) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        return null;
    }
}
