package net.yura.shithead.client;

import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

public class ShitHeadClient extends Application implements ActionListener {

    protected void initialize(DesktopPane dp) {

        dp.setLookAndFeel(DesktopPane.getSystemLookAndFeelClassName());

        try {

            ResourceBundle bundle = ResourceBundle.getBundle("game_text");
            Properties properties = new Properties() {
                @Override
                public String getProperty(String key) {
                    return bundle.getString(key);
                }
            };

            XULLoader loader = new XULLoader();
            try (InputStream stream = ShitHeadClient.class.getResourceAsStream("/main_menu.xml")) {
                loader.load(new InputStreamReader(stream), this, properties);
            }

            Frame frame = (Frame)loader.getRoot();
            frame.setMaximum(true);
            frame.setVisible(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void actionPerformed(String actionCommand) {
        if ("play".equals(actionCommand)) {
            System.out.println("Play Online clicked");
        }
        else if ("about".equals(actionCommand)) {
            System.out.println("About clicked");
        }
    }
}