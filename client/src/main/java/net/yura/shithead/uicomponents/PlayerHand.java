package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import java.util.ArrayList;
import java.util.Collections;
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
    final private boolean isLocalPlayer;
    private boolean isWaitingForInput = false;
    private static final int padding = XULLoader.adjustSizeToDensity(2);

    public PlayerHand(ShitheadGame game, Player player, boolean isLocalPlayer, GameViewListener gameCommandListener) {
        this.game = game;
        this.player = player;
        this.gameCommandListener = gameCommandListener;
        this.isLocalPlayer = isLocalPlayer;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWaitingForInput(boolean isCurrentPlayer) {
        this.isWaitingForInput = isCurrentPlayer;
    }

    public boolean isWaitingForInput() {
        return isWaitingForInput;
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
        Card top = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            UICard uiCard = new UICard(card, location, isFaceUp);
            if (card != null && isLocalPlayer && isWaitingForInput && isFaceUp) {
                boolean activePile = (!player.getHand().isEmpty() && location == CardLocation.HAND) ||
                        (player.getHand().isEmpty() && !player.getUpcards().isEmpty() && location == CardLocation.UP_CARDS);
                if (activePile) {
                    uiCard.setPlayable(game.isPlayable(card.getRank(), top));
                }
            }
            uiCard.setPosition(x + startX + i * (CardImageManager.cardWidth + padding), y + yOffset);
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
        if (isWaitingForInput) {
            g.setColor(0xFF00FF00); // Green
            int arrowWidth = XULLoader.adjustSizeToDensity(10);
            int arrowHeight = XULLoader.adjustSizeToDensity(15);
            g.fillTriangle(0,0, -arrowWidth, -arrowHeight, arrowWidth, -arrowHeight);
        }
        g.setColor(0xFF000000);
        g.drawString(player.getName(), -g.getFont().getWidth(player.getName()) / 2, -g.getFont().getHeight());
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
                    else if (!game.isFinished()) {
                        Player player = game.getCurrentPlayer();
                        if (player.getHand().isEmpty() && player.getUpcards().isEmpty()) {
                            // TODO do we want to be able to pick the index of the downcard we play
                            gameCommandListener.playDowncard();
                        }
                        else if (uiCard.isPlayable()) {
                            // TODO if we have 2 of the same rank, we want to ONLY select it, so we can then play more then 1 at a time
                            CardLocation location = uiCard.getLocation();
                            long sameRank = uiCards.stream().filter(c -> c.getLocation() == location).filter(c -> c.getCard().getRank() == uiCard.getCard().getRank()).count();
                            if (sameRank == 1) {
                                uiCards.forEach(c -> c.setSelected(false)); // deselect all
                                gameCommandListener.playVisibleCard(uiCard.getLocation() == CardLocation.HAND, Collections.singletonList(uiCard.getCard()));
                            }
                            else {
                                // deselect all cards that are NOT this rank, when we have multiple of multiple ranks
                                uiCards.stream().filter(c -> c.getCard().getRank() != uiCard.getCard().getRank()).forEach(c -> c.setSelected(false));
                                // if there is more then 1 then we just toggle the selection
                                uiCard.toggleSelection();
                                gameCommandListener.updateButton();
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
