package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.yura.cardsengine.Card;
import net.yura.shithead.common.ShitheadGame;

public class PlayerHand {

    private GameViewListener gameCommandListener;

    private final ShitheadGame game;
    final Player player;
    private final List<UICard> uiCards = new ArrayList<UICard>();
    int x;
    int y;
    private boolean isCurrentPlayer = false;
    private static final int padding = XULLoader.adjustSizeToDensity(2);

    public PlayerHand(ShitheadGame game, Player player, GameViewListener gameCommandListener) {
        this.game = game;
        this.player = player;
        this.gameCommandListener = gameCommandListener;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCurrentPlayer(boolean isCurrentPlayer) {
        this.isCurrentPlayer = isCurrentPlayer;
    }

    public void addCard(UICard card) {
        uiCards.add(card);
    }

    public void clear() {
        uiCards.clear();
    }

    public void layoutHand(CardLocation location, List<Card> cards, int yOffset, boolean isFaceUp) {
        int handWidth = (cards.size() * CardImageManager.cardWidth) + (padding * (cards.size() - 1));
        int startX = - handWidth / 2;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            UICard uiCard = new UICard(card, this.player, location, isFaceUp);
            uiCard.setPosition(startX + i * (CardImageManager.cardWidth + padding), yOffset);
            addCard(uiCard);
        }
    }

    public List<UICard> getUiCards() {
        return uiCards;
    }

    private List<UICard> getSelectedUiCards() {
        return uiCards.stream().filter(UICard::isSelected).collect(Collectors.toList());
    }

    public void paint(Graphics2D g, Component c) {
        if (isCurrentPlayer) {
            g.setColor(0xFF00FF00); // Green
            int arrowWidth = XULLoader.adjustSizeToDensity(10);
            int arrowHeight = XULLoader.adjustSizeToDensity(15);
            g.fillTriangle(0,0, -arrowWidth, -arrowHeight, arrowWidth, -arrowHeight);
        }
        for (UICard card : uiCards) {
            card.paint(g, c);
        }
    }

    public boolean processMouseEvent(int type, int x, int y, net.yura.mobile.gui.KeyEvent buttons) {
        if (type == net.yura.mobile.gui.DesktopPane.RELEASED) {
            for (int i = uiCards.size() - 1; i >= 0; i--) {
                UICard uiCard = uiCards.get(i);
                if (uiCard.contains(x, y)) {
                    if (game.isRearranging()) {
                        List<UICard> selected = getSelectedUiCards();
                        if (selected.isEmpty() || uiCard == selected.get(0)) {
                            uiCard.toggleSelection();
                        }
                        else if (selected.size() == 1) {
                            selected.get(0).toggleSelection();
                            if (selected.get(0).getLocation() == uiCard.getLocation()) {
                                uiCard.toggleSelection();
                            }
                            else if (selected.get(0) != uiCard) {
                                gameCommandListener.swapCards(uiCard.getCard(), selected.get(0).getCard());
                            }
                        }
                        else {
                            System.out.println("too many cards selected???? " + selected);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
