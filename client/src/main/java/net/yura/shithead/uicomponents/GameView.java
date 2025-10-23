package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Panel;
import java.util.List;
import net.yura.cardsengine.Card;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;

public class GameView extends Panel {

    private ShitheadGame game;
    private String myUsername;
    private final int padding = XULLoader.adjustSizeToDensity(2);
    private final int cardWidth = CardImageManager.getCardBackImage().getIconWidth();
    private final int cardHeight = CardImageManager.getCardBackImage().getIconHeight();

    public GameView() {
    }

    public void setGame(ShitheadGame game) {
        this.game = game;
        repaint();
    }

    public void setPlayerID(String playerID) {
        this.myUsername = playerID;
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);
        if (game == null) {
            return;
        }

        List<Player> players = game.getPlayers();
        int localPlayerIndex = 0; // TODO fix hard coded

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
        int overlap = XULLoader.adjustSizeToDensity(20);

        boolean isLocalPlayer = position == 0;

        if (isLocalPlayer) {
            int x = centerX;
            int y = height - cardHeight;
            drawHand(g, player.getDowncards(), x, y - padding - overlap*2,  false, false);
            drawHand(g, player.getUpcards(), x, y - padding - overlap,  false, true);
            drawHand(g, player.getHand(), x, y,  false, true);
        } else {
            int otherPlayerCount = playerCount - 1;
            if (otherPlayerCount > 0) {
                double angle = Math.PI + (Math.PI * position / (otherPlayerCount + 1));
                int x = centerX + (int) (radiusX * Math.cos(angle));
                int y = centerY + (int) (radiusY * Math.sin(angle));
                drawHand(g, player.getDowncards(), x, y, false, false);
                drawHand(g, player.getUpcards(), x, y + overlap,false, true);
                drawHand(g, player.getHand(), x, y + overlap*2, false, false);
            }
        }
    }

    private void drawHand(Graphics2D g, List<Card> hand, int x, int y, boolean stack, boolean isFaceUp) {

        x -= ((hand.size() * cardWidth) + (padding * (hand.size() - 1)))/2;

        for (int i = 0; i < hand.size(); i++) {
            drawCard(g, hand.get(i), x + i * (cardWidth + padding), y, isFaceUp);
        }
    }

    private void drawCard(Graphics2D g, Card card, int x, int y, boolean isFaceUp) {
        Icon icon;
        boolean isVisible = isFaceUp && card != null;

        if (isVisible) {
            icon = CardImageManager.getCardImage(card);
        } else {
            icon = CardImageManager.getCardBackImage();
        }

        if (icon != null) {
            icon.paintIcon(this, g, x, y);
        } else {
            g.setColor(0xFF808080); // Gray
            g.fillRect(x, y, 71, 96);
        }
    }
}
