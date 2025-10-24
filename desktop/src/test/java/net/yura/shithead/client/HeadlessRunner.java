package net.yura.shithead.client;

import net.yura.mobile.gui.Application;
import org.me4se.JadFile;
import org.mockito.Mockito;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.microedition.midlet.ApplicationManager;
import javax.swing.JPanel;

import static org.mockito.ArgumentMatchers.anyInt;

public class HeadlessRunner {

    public static Component runApplication(Class<? extends Application> appClass) {

        System.setProperty("java.awt.headless", "true");

        Graphics2D mockGraphics = Mockito.mock(Graphics2D.class, Mockito.withSettings().stubOnly());
        Mockito.when(mockGraphics.create()).thenReturn(mockGraphics);
        Mockito.when(mockGraphics.create(anyInt(),anyInt(),anyInt(),anyInt())).thenReturn(mockGraphics);

        JPanel offScreenPanel = new JPanel() {
            /**
             * @see org.me4se.scm.ScmWrapper#update(Graphics)
             */
            @Override
            public boolean isShowing() {
                return true;
            }

            @Override
            public void repaint(long tm, int x, int y, int width, int height) {
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getComponentCount() > 0) {
                                            // we specifically call update, as thats the method thats called if repaint was requested in an awt component
                                            getComponent(0).update(mockGraphics);
                                        }
                                    }
                                }));
            }
        };
        offScreenPanel.setSize(320, 560);

        ApplicationManager manager = ApplicationManager.createInstance(offScreenPanel, null);

        offScreenPanel.doLayout();

        Component comp = offScreenPanel.getComponent(0);
        sun.awt.AWTAccessor.getComponentAccessor().setPeer(offScreenPanel, new sun.awt.NullComponentPeer());
        sun.awt.AWTAccessor.getComponentAccessor().setPeer(comp, new sun.awt.NullComponentPeer());

        JadFile jad = new JadFile();
        jad.setValue("MIDlet-1", ",," + appClass.getName());
        manager.launch(jad, 0);

        comp.repaint(); // this is how app requested repaints will come in

        return comp;
    }

    public static void saveToFile(Component offScreenPanel, File file) {
        BufferedImage img = new BufferedImage(offScreenPanel.getWidth(), offScreenPanel.getHeight(), BufferedImage.TYPE_INT_BGR);
        Graphics g = img.getGraphics();
        // we HAVE to set the clip as the ScmWrapper expects a clip to always be set, TODO remove for next version of me4se
        g.setClip(0,0, img.getWidth(), img.getHeight());
        offScreenPanel.paint(g);
        g.dispose();

        try {
            boolean result = ImageIO.write(img, "png", file);
            if (!result) {
                throw new RuntimeException("failed to save, result = false");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
