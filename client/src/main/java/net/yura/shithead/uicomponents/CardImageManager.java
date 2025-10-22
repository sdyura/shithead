package net.yura.shithead.uicomponents;

import java.util.HashMap;
import java.util.Map;
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
            Icon icon = new Icon("/cards/" + cardName + ".gif");
            cardImages.put(cardName, icon);
        }
        return cardImages.get(cardName);
    }

    public static Icon getCardBackImage() {
        String cardName = "1z";
        if (!cardImages.containsKey(cardName)) {
            Icon icon = new Icon("/cards/" + cardName + ".gif");
            cardImages.put(cardName, icon);
        }
        return cardImages.get(cardName);
    }

    private static String getCardName(Card card) {
        return "" + card.getRank().toInt() + Character.toLowerCase(card.getSuit().toChar());
    }
}