package net.yura.shithead.uicomponents;

import javax.microedition.lcdui.Graphics;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Panel;
import java.util.List;
import net.yura.cardsengine.Card;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;

public class GameView extends Panel {

    private ShitheadGame game;
    private int playerID;
    private boolean spectatorView = false;

    public GameView() {
    }

    public void setGame(ShitheadGame game) {
        this.game = game;
        repaint();
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public void setSpectatorView(boolean spectatorView) {
        this.spectatorView = spectatorView;
        repaint();
    }

    public boolean isSpectatorView() {
        return spectatorView;
    }

    public void paint(Graphics g) {
        if (game == null) {
            return;
        }

        Graphics2D g2 = new Graphics2D(g);

        List<Player> players = game.getPlayers();
        int localPlayerIndex = playerID;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int playerPosition = (i - localPlayerIndex + players.size()) % players.size();
            drawPlayer(g2, player, playerPosition, players.size());
        }
    }

    private void drawPlayer(Graphics2D g, Player player, int position, int playerCount) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radiusX = width / 2 - 50;
        int radiusY = height / 2 - 50;

        boolean isLocalPlayer = position == 0;

        if (isLocalPlayer) {
            int x = centerX;
            int y = height - 100;
            drawHand(g, player.getHand(), x, y, 20, 0, true, true);
        } else {
            int otherPlayerCount = playerCount - 1;
            if (otherPlayerCount > 0) {
                double angle = Math.PI + (Math.PI * position / (otherPlayerCount + 1));
                int x = centerX + (int) (radiusX * Math.cos(angle));
                int y = centerY + (int) (radiusY * Math.sin(angle));
                drawHand(g, player.getHand(), x, y, 0, 15, false, false);
            }
        }
    }

    private void drawHand(Graphics2D g, List<Card> hand, int x, int y, int dx, int dy, boolean center, boolean isLocalPlayer) {
        if (center) {
            x -= (hand.size() * dx) / 2;
        }
        for (int i = 0; i < hand.size(); i++) {
            drawCard(g, hand.get(i), x + i * dx, y + i * dy, isLocalPlayer);
        }
    }

    private void drawCard(Graphics2D g, Card card, int x, int y, boolean isLocalPlayer) {
        Icon icon;
        boolean isVisible = !spectatorView && isLocalPlayer && card != null;

        if (isVisible) {
            icon = CardImageManager.getCardImage(card);
        } else {
            icon = CardImageManager.getCardBackImage();
        }

        if (icon != null) {
            icon.paintIcon(this, g, x, y);
        } else {
            g.setColor(0x808080); // Gray
            g.fillRect(x, y, 71, 96);
        }
    }
}