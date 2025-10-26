package net.yura.shithead.uicomponents;

import java.util.HashMap;
import java.util.Map;
import net.yura.lobby.mini.GameRenderer;
import net.yura.mobile.gui.Icon;
import net.yura.cardsengine.Card;

public class CardImageManager {

    private static final Map<String, Icon> cardImages = new HashMap<>();

    public static Icon getCardImage(Card card) {
        if (card == null) {
            return getCardBackImage();
        }
        String cardName = getCardName(card);
        if (!cardImages.containsKey(cardName)) {
            Icon img = new Icon("/cards/" + cardName + ".gif");
            GameRenderer.ScaledIcon icon = new GameRenderer.ScaledIcon(img.getIconWidth() / 2, img.getIconHeight() / 2);
            icon.setIcon(img);
            cardImages.put(cardName, icon);
        }
        return cardImages.get(cardName);
    }

    public static Icon getCardBackImage() {
        String cardName = "1z"; // this is the joker
        if (!cardImages.containsKey(cardName)) {
            Icon img = new Icon("/cards/" + cardName + ".gif");
            GameRenderer.ScaledIcon icon = new GameRenderer.ScaledIcon(img.getIconWidth() / 2, img.getIconHeight() / 2);
            icon.setIcon(new CardBack(icon.getIconWidth(), icon.getIconHeight()));
            cardImages.put(cardName, icon);
        }
        return cardImages.get(cardName);
    }

    private static String getCardName(Card card) {
        return "" + card.getRank().toInt() + Character.toLowerCase(card.getSuit().toChar());
    }
}