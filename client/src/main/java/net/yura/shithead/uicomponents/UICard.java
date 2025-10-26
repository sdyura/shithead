package net.yura.shithead.uicomponents;

import net.yura.cardsengine.Card;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;
import net.yura.shithead.common.Player;

public class UICard {

    private Card card;
    private Player player;
    private CardLocation location;
    private boolean faceUp;
    private int x;
    private int y;

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
    }
}
