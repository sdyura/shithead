package net.yura.shithead.client;

import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ShitHeadClient extends Application implements ActionListener {

    private Properties properties;

    protected void initialize(DesktopPane dp) {

        dp.setLookAndFeel(DesktopPane.getSystemLookAndFeelClassName());

        try {

            ResourceBundle bundle = ResourceBundle.getBundle("game_text");
            properties = new Properties() {
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
            String versionName = System.getProperty("versionName");
            String versionCode = System.getProperty("versionCode");

            if (versionName == null) {
                try (InputStream stream = Application.getResourceAsStream("/META-INF/MANIFEST.MF")) {
                    Manifest manifest = new Manifest(stream);
                    Attributes attributes = manifest.getMainAttributes();
                    versionName = attributes.getValue("versionName");
                    versionCode = attributes.getValue("versionCode");
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            OptionPane.showMessageDialog(null, new String[] {"Version: " + versionName, "Build: " + versionCode}, properties.getProperty("about.title"), OptionPane.INFORMATION_MESSAGE);
        }
    }
}
