package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import java.util.ArrayList;
import java.util.List;
import net.yura.cardsengine.Card;

public class PlayerHand {
    private Player player;
    private List<UICard> uiCards = new ArrayList<UICard>();
    int x;
    int y;
    private boolean isCurrentPlayer = false;
    private static final int padding = XULLoader.adjustSizeToDensity(2);

    public PlayerHand(Player player) {
        this.player = player;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCurrentPlayer(boolean isCurrentPlayer) {
        this.isCurrentPlayer = isCurrentPlayer;
    }

    public void addCard(UICard card) {
        uiCards.add(card);
    }

    public void clear() {
        uiCards.clear();
    }

    public void layoutHand(CardLocation location, List<Card> cards, int yOffset, boolean isFaceUp) {
        int handWidth = (cards.size() * CardImageManager.cardWidth) + (padding * (cards.size() - 1));
        int startX = - handWidth / 2;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            UICard uiCard = new UICard(card, this.player, location, isFaceUp);
            uiCard.setPosition(startX + i * (CardImageManager.cardWidth + padding), yOffset);
            addCard(uiCard);
        }
    }

    public List<UICard> getUiCards() {
        return uiCards;
    }

    public void paint(Graphics2D g, Component c) {
        if (isCurrentPlayer) {
            g.setColor(0xFF00FF00); // Green
            int arrowWidth = XULLoader.adjustSizeToDensity(10);
            int arrowHeight = XULLoader.adjustSizeToDensity(15);
            g.fillTriangle(0,0, -arrowWidth, -arrowHeight, arrowWidth, -arrowHeight);
        }
        for (UICard card : uiCards) {
            card.paint(g, c);
        }
    }

    public boolean processMouseEvent(int type, int x, int y, net.yura.mobile.gui.KeyEvent buttons) {
        if (type == net.yura.mobile.gui.DesktopPane.RELEASED) {
            for (int i = uiCards.size() - 1; i >= 0; i--) {
                UICard uiCard = uiCards.get(i);
                if (uiCard.contains(x, y)) {
                    uiCard.toggleSelection();
                    return true;
                }
            }
        }
        return false;
    }
}
