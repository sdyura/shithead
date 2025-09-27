package net.yura.shithead.client;

import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.Panel;
import javax.microedition.lcdui.Graphics;

public class ShitHeadClient extends Application {

    protected void initialize(DesktopPane dp) {

        dp.setLookAndFeel(DesktopPane.getSystemLookAndFeelClassName());

        Frame frame = new Frame("SwingME app");

        frame.getContentPane().add(new Label("Hello ShitHead"));

        Button exit = new Button("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(String arg0) {
                Application.exit();
            }
        });
        exit.setMnemonic(KeyEvent.KEY_END); // press Esc to exit

        Panel panel = new Panel();
        panel.add(exit);
        frame.getContentPane().add(panel, Graphics.BOTTOM);

        //frame.pack(); // shrink to the smallest size
        //frame.setLocationRelativeTo(null); // center

        frame.setMaximum(true);
        frame.setVisible(true);
    }
}
