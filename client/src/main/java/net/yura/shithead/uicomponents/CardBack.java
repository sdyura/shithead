package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;
import javax.microedition.lcdui.Image;

public class CardBack extends Icon {

    private Image img;
    private int color;

    public CardBack(int width, int height) {
        this.width = width;
        this.height = height;

        img = Application.createImage("/back.png");
        //int[] color = new int[1];
        //img.getRGB(color, 0, 1, 0,0, 1, 1);
        //this.color = color[0];
        this.color = 0xFFc1c1c1;
    }

    @Override
    public void paintIcon(Component c, Graphics2D g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, width, height);

        double s = Math.min(height / (double) img.getHeight(), width / (double) img.getWidth());
        int dw = (int) (img.getWidth() * s);
        int dh = (int) (img.getHeight() * s);
        g.drawScaledImage(img, x + (width - dw) / 2, y + (height - dh) / 2, dw, dh);

        g.setColor(0xFF000000);
        int old = g.getGraphics().getStrokeWidth();
        g.getGraphics().setStrokeWidth(XULLoader.adjustSizeToDensity(1));
        g.drawRect(x, y, width, height);
        g.getGraphics().setStrokeWidth(old);
    }
}
