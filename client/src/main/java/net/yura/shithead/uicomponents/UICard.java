package net.yura.shithead.uicomponents;

import net.yura.cardsengine.Card;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.gui.border.LineBorder;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;

public class UICard {

    private Card card;
    private CardLocation location;
    private boolean faceUp;
    private int x, y;
    private int targetX, targetY;
    private boolean selected;
    private boolean playable;

    private static final Border selectionBorder = new LineBorder(0xFFFF0000, 2);
    private static final Border playableBorder = new LineBorder(0xFFFFBF00, 2);

    public UICard(Card card, CardLocation location, boolean faceUp) {
        this.card = card;
        this.location = location;
        this.faceUp = faceUp;
    }

    public void setPosition(int x, int y) {
        this.targetX = x;
        this.targetY = y;
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
        } else if (playable) {
            g.translate(x, y);
            playableBorder.paintBorder(c, g, icon.getIconWidth(), icon.getIconHeight());
            g.translate(-x, -y);
        }
    }

    public Card getCard() {
        return card;
    }

    public CardLocation getLocation() {
        return location;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelection() {
        selected = !selected;
    }

    public void setPlayable(boolean isPlayable) {
        this.playable = isPlayable;
    }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + CardImageManager.cardWidth &&
               py >= y && py <= y + CardImageManager.cardHeight;
    }

    public boolean animate() {
        boolean more = false;
        if (x != targetX || y != targetY) {
            double[] pos = moveToward(x, y, targetX, targetY);
            x = (int)pos[0];
            y = (int)pos[1];

            more = true;
        }

        return more;
    }

    public static double[] moveToward(double x, double y, double targetX, double targetY) {

        double dx = targetX - x;
        double dy = targetY - y;

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Already at target
        if (distance == 0) {
            return new double[]{x, y};
        }

        // Step size logic
        double maxStep = XULLoader.adjustSizeToDensity(10);
        double minStep = 1.0;

        // Slow down as we get close
        double step = Math.min(maxStep, distance);
        if (step < minStep) {
            step = minStep;
        }

        // Never overshoot
        if (step > distance) {
            step = distance;
        }

        // Normalize direction
        double nx = dx / distance;
        double ny = dy / distance;

        // Move
        x += nx * step;
        y += ny * step;

        return new double[]{x, y};
    }
}
