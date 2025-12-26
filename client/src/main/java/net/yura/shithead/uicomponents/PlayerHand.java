package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import java.util.ArrayList;
import java.util.List;
import net.yura.cardsengine.Card;

public class PlayerHand {
    private Player player;
    private List<UICard> uiCards = new ArrayList<UICard>();
    private int x;
    private int y;

    public PlayerHand(Player player) {
        this.player = player;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void paint(Graphics2D g, Component c) {
        for (int i = 0; i < uiCards.size(); i++) {
            uiCards.get(i).paint(g, c);
        }
    }

    public void addCard(UICard card) {
        uiCards.add(card);
    }

    public void clear() {
        uiCards.clear();
    }

    public void layoutHand(CardLocation location, List<Card> cards, int yOffset, boolean isFaceUp, int padding) {
        int handWidth = (cards.size() * CardImageManager.cardWidth) + (padding * (cards.size() - 1));
        int startX = this.x - handWidth / 2;
        int startY = this.y + yOffset;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            UICard uiCard = new UICard(card, this.player, location, isFaceUp);
            uiCard.setPosition(startX + i * (CardImageManager.cardWidth + padding), startY);
            addCard(uiCard);
        }
    }

    public List<UICard> getUiCards() {
        return uiCards;
    }
}
