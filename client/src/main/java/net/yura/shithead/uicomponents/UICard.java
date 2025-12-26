package net.yura.shithead.uicomponents;

import net.yura.cardsengine.Card;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.gui.border.LineBorder;
import net.yura.mobile.gui.components.Component;
import net.yura.shithead.common.Player;

public class UICard {

    private Card card;
    private Player player;
    private CardLocation location;
    private boolean faceUp;
    private int x;
    private int y;
    private boolean selected;

    private static final Border selectionBorder = new LineBorder(0xFFFF0000, 2);

    public UICard(Card card, Player player, CardLocation location, boolean faceUp) {
        this.card = card;
        this.player = player;
        this.location = location;
        this.faceUp = faceUp;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void paint(Graphics2D g, Component c) {
        Icon icon;
        if (faceUp && card != null) {
            icon = CardImageManager.getCardImage(card);
        } else {
            icon = CardImageManager.getCardBackImage();
        }

        icon.paintIcon(c, g, x, y);
        if (selected) {
            g.translate(x, y);
            selectionBorder.paintBorder(c, g, icon.getIconWidth(), icon.getIconHeight());
            g.translate(-x, -y);
        }
    }

    public Card getCard() {
        return card;
    }

    public Player getPlayer() {
        return player;
    }

    public CardLocation getLocation() {
        return location;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelection() {
        selected = !selected;
    }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + CardImageManager.cardWidth &&
               py >= y && py <= y + CardImageManager.cardHeight;
    }
}
