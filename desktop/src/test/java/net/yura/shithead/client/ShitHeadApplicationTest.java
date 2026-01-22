package net.yura.shithead.client;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.MenuBar;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.TextComponent;
import net.yura.mobile.gui.components.TextPane;
import net.yura.mobile.gui.plaf.Style;
import net.yura.mobile.gui.plaf.nimbus.NimbusLookAndFeel;
import net.yura.mobile.util.RemoteTest;
import org.junit.jupiter.api.Test;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Image;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class ShitHeadApplicationTest {

    public static class TestApp extends ShitHeadApplication {
        @Override
        protected void setupTheme(DesktopPane dp) {
            try {
                // font looks different on different OSs, we use a bitmap font so it looks the same everywhere
                Font font = Font.getFont(getResourceAsStream("/bitmap_font.fnt"),new Image[] {createImage("/bitmap_font.png")},new int[] {0xFF000000});
                Hashtable<String, Font> themeSettings = new Hashtable<>();
                themeSettings.put("font", font);
                dp.setLookAndFeel(new NimbusLookAndFeel(javax.microedition.lcdui.Font.SIZE_MEDIUM, themeSettings));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void doTest() throws Exception {

        Component comp = HeadlessRunner.runApplication(TestApp.class);

        // wait for frame to be open
        await().atMost(5, TimeUnit.SECONDS).until(() -> !DesktopPane.getDesktopPane().getAllFrames().isEmpty());
        // wait for all
        EventQueue.invokeAndWait(new Thread());

            // TODO temp code to use predictable bitmap font in test
            net.yura.mobile.gui.components.Component title = getComponentByText(DesktopPane.getDesktopPane().getSelectedFrame(), "Shithead!");
            // reset to default font
            ((Label)title).setForeground(DesktopPane.getDefaultTheme(title).getForeground(Style.ALL));
            ((Label)title).setFont(DesktopPane.getDefaultTheme(title).getFont(Style.ALL));
            // we need to tell it that we want to revalidate and repaint, otherwise it will just use old cached buffer
            DesktopPane.getDesktopPane().getSelectedFrame().revalidate();
            DesktopPane.getDesktopPane().getSelectedFrame().repaint();
            // wait for all
            EventQueue.invokeAndWait(new Thread());

        // TODO this click is working (needs new SwingME lib version), but UI does not seem to update
        //boolean click = RemoteTest.clickText(DesktopPane.getDesktopPane().getSelectedFrame(), "Single player");
        //assertTrue(click);

        File outputFolder = new File("testOutput");
        outputFolder.mkdir();

        File testOutput = new File(outputFolder, "main_menu.png");
        HeadlessRunner.saveToFile(comp, testOutput);

        File img2 = new File("src/test/resources/screens/main_menu.png");

        checkImagesMatch(testOutput, img2);
    }

    /**
     * TODO prob would be good to move this to RemoteTest
     * @see RemoteTest
     */
    public static net.yura.mobile.gui.components.Component getComponentByText(net.yura.mobile.gui.components.Component comp, String text) {
        if (comp instanceof Panel) {
            for (net.yura.mobile.gui.components.Component child : (List<net.yura.mobile.gui.components.Component>) ((Panel) comp).getComponents()) {
                net.yura.mobile.gui.components.Component result = getComponentByText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        else if (comp instanceof MenuBar) {
            for (net.yura.mobile.gui.components.Component child : (List<net.yura.mobile.gui.components.Component>) ((MenuBar) comp).getItems()) {
                net.yura.mobile.gui.components.Component result = getComponentByText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        else if (comp instanceof Label) {
            if (text.equalsIgnoreCase(((Label) comp).getText())) {
                return comp;
            }
        }
        else if (comp instanceof TextComponent) {
            if (text.equalsIgnoreCase(((TextComponent) comp).getText())) {
                return comp;
            }
        }
        else if (comp instanceof TextPane) {
            if (text.equalsIgnoreCase(((TextPane) comp).getText())) {
                return comp;
            }
        }
        return null;
    }

    public static void checkImagesMatch(File f1, File f2) {
        BufferedImage img1,img2;
        try {
            img1 = ImageIO.read(f1);
        }
        catch (IOException e) {
            throw new RuntimeException("error reading " + f1, e);
        }

        try {
            img2 = ImageIO.read(f2);
        }
        catch (IOException e) {
            throw new RuntimeException("error reading " + f2, e);
        }

        // Compare images with tolerance
        ImageComparisonResult result = new ImageComparison(img1, img2)
                .setPixelToleranceLevel(0.1) // Allow up to 10% pixel difference
                .compareImages();

        // Assert similarity
        assertEquals(ImageComparisonState.MATCH, result.getImageComparisonState(), "Images are not similar!");
    }
}
