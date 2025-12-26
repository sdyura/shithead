package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Component;
import java.util.ArrayList;
import java.util.List;

public class CardPile {
    private List<UICard> uiCards = new ArrayList<UICard>();

    public void addCard(UICard card) {
        uiCards.add(card);
    }

    public void clear() {
        uiCards.clear();
    }

    public void paint(Graphics2D g, Component c) {
        for (int i = 0; i < uiCards.size(); i++) {
            uiCards.get(i).paint(g, c);
        }
    }

    public List<UICard> getUiCards() {
        return uiCards;
    }
}
