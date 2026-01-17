package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;

public class Icons extends Icon {

    private final Icon[] icons;

    public Icons(Icon[] icons) {
        this.icons = icons;
    }

    @Override
    public int getIconWidth() {
        int width = 0;
        for (Icon icon : icons) {
            width += icon.getIconWidth();
        }
        return width;
    }

    @Override
    public int getIconHeight() {
        return icons[0].getIconHeight();
    }

    @Override
    public void paintIcon(Component c, Graphics2D g, int x, int y) {
        for (Icon icon : icons) {
            icon.paintIcon(c, g, x, y);
            x += icon.getIconWidth();
        }
    }
}
