package net.yura.shithead.client;

import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ShitHeadClient extends Application implements ActionListener {

    protected void initialize(DesktopPane dp) {

        dp.setLookAndFeel(DesktopPane.getSystemLookAndFeelClassName());

        try {
            XULLoader loader = new XULLoader();

            ResourceBundle bundle = ResourceBundle.getBundle("net.yura.shithead.client.ui.game_text");
            Properties properties = new Properties();
            for (Enumeration<String> e = bundle.getKeys(); e.hasMoreElements(); ) {
                String key = e.nextElement();
                properties.put(key, bundle.getString(key));
            }

            InputStream stream = ShitHeadClient.class.getResourceAsStream("/net/yura/shithead/client/ui/lobby.xml");

            loader.load(stream, this, properties);
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