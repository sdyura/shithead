package net.yura.shithead.uicomponents;

import java.util.HashMap;
import java.util.Map;
import net.yura.lobby.mini.GameRenderer;
import net.yura.mobile.gui.Icon;
import net.yura.cardsengine.Card;
import net.yura.mobile.gui.layout.XULLoader;

public class CardImageManager {

    // at mdpi, the size of the card is half the image size
    // the images in the lib ARE already @2x, but they do not have it in the name
    // todo we may want to make it bigger as minimum touch target is 44 dip according to apple
    public static final int cardWidth = XULLoader.adjustSizeToDensity(66 / 2);
    public static final int cardHeight = XULLoader.adjustSizeToDensity(120 / 2);

    private static final Map<String, Icon> cardImages = new HashMap<>();

    public static Icon getCardImage(Card card) {
        if (card == null) {
            return getCardBackImage();
        }
        String cardName = getCardName(card);
        if (!cardImages.containsKey(cardName)) {
            GameRenderer.ScaledIcon icon = new GameRenderer.ScaledIcon(cardWidth, cardHeight);
            icon.setIcon(new Icon("/cards/" + cardName + ".gif"));
            cardImages.put(cardName, icon);
        }
        return cardImages.get(cardName);
    }

    public static Icon getCardBackImage() {
        String cardName = "back";
        if (!cardImages.containsKey(cardName)) {
            Icon icon = new CardBack(cardWidth, cardHeight);
            cardImages.put(cardName, icon);
        }
        return cardImages.get(cardName);
    }

    private static String getCardName(Card card) {
        return "" + card.getRank().toInt() + Character.toLowerCase(card.getSuit().toChar());
    }
}