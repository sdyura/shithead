package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;

public class CardBack extends Icon {

    public CardBack(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics2D g, int x, int y) {
        g.setColor(0xFF808080);
        g.fillRect(x, y, width, height);
        g.setColor(0xFF000000);
        int old = g.getGraphics().getStrokeWidth();
        g.getGraphics().setStrokeWidth(XULLoader.adjustSizeToDensity(1));
        g.drawRect(x, y, width, height);
        g.getGraphics().setStrokeWidth(old);
    }
}
