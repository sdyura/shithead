package net.yura.shithead.client;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.plaf.nimbus.NimbusLookAndFeel;
import org.junit.jupiter.api.Test;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
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
            } catch (IOException e) {
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

        //RemoteTest.open();

        File outputFolder = new File("testOutput");
        outputFolder.mkdir();

        File testOutput = new File(outputFolder, "main_menu.png");
        HeadlessRunner.saveToFile(comp, testOutput);

        File img2 = new File("src/test/resources/screens/main_menu.png");

        checkImagesMatch(testOutput, img2);
    }

    public static void checkImagesMatch(File f1, File f2) {
        BufferedImage img1,img2;
        try {
            img1 = ImageIO.read(f1);
        } catch (IOException e) {
            throw new RuntimeException("error reading " + f1, e);
        }

        try {
            img2 = ImageIO.read(f2);
        } catch (IOException e) {
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