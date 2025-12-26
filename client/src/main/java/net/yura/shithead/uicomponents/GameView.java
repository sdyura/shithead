package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.GridBagLayout;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.yura.cardsengine.Card;

public class GameView extends Panel {
    private ShitheadGame game;
    private String myUsername;
    private final CardPile deckPile = new CardPile();
    private final CardPile wastePile = new CardPile();
    private final Map<Player, PlayerHand> playerHands = new HashMap<Player, PlayerHand>();
    private final int padding = XULLoader.adjustSizeToDensity(2);

    public GameView() {
    }

    public void setGame(ShitheadGame game) {
        this.game = game;
        layoutCards();
        repaint();
    }

    public void setPlayerID(String playerID) {
        this.myUsername = playerID;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        layoutCards();
    }

    @Override
    public void paintComponent(Graphics2D g) {
        super.paintComponent(g);
        for (PlayerHand hand : playerHands.values()) {
            hand.paint(g, this);
        }
        deckPile.paint(g, this);
        wastePile.paint(g, this);
    }

    private void layoutCards() {
        deckPile.clear();
        wastePile.clear();
        playerHands.clear();
        if (game == null) {
            return;
        }

        List<Player> players = game.getPlayers();
        int localPlayerIndex = 0; // TODO fix hard coded

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int playerPosition = (i - localPlayerIndex + players.size()) % players.size();
            layoutPlayer(player, playerPosition, players.size());
        }

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Deck
        int deckSize = game.getDeck().getCards().size();
        int cardsToShowDeck = Math.min(deckSize, 3);
        if (cardsToShowDeck > 0) {
            int stackHeightDeck = CardImageManager.cardHeight + (cardsToShowDeck - 1) * padding;
            int yStartDeck = centerY - stackHeightDeck / 2;
            for (int i = 0; i < cardsToShowDeck; i++) {
                UICard deckCard = new UICard(null, null, CardLocation.DECK, false);
                deckCard.setPosition(centerX - CardImageManager.cardWidth - padding / 2, yStartDeck + i * padding);
                deckPile.addCard(deckCard);
            }
        }

        // Waste Pile
        List<Card> gameWastePile = game.getWastePile();
        int wastePileSize = gameWastePile.size();
        int cardsToShowWaste = Math.min(wastePileSize, 3);
        if (cardsToShowWaste > 0) {
            int stackHeightWaste = CardImageManager.cardHeight + (cardsToShowWaste - 1) * padding;
            int yStartWaste = centerY - stackHeightWaste / 2;
            for (int i = 0; i < cardsToShowWaste; i++) {
                Card card = gameWastePile.get(wastePileSize - cardsToShowWaste + i);
                UICard wastePileCard = new UICard(card, null, CardLocation.WASTE, true);
                wastePileCard.setPosition(centerX + padding / 2, yStartWaste + i * padding);
                wastePile.addCard(wastePileCard);
            }
        }
    }

    private void layoutPlayer(Player player, int position, int playerCount) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radiusX = width / 2 - 50;
        int radiusY = height / 2 - 50;
        int overlap = XULLoader.adjustSizeToDensity(20);

        PlayerHand hand = new PlayerHand(player);
        playerHands.put(player, hand);

        boolean isLocalPlayer = position == 0;

        if (isLocalPlayer) {
            hand.setPosition(centerX, height - CardImageManager.cardHeight - padding - overlap * 2);
            hand.layoutHand(CardLocation.DOWN_CARDS, player.getDowncards(), 0, false, padding);
            hand.layoutHand(CardLocation.UP_CARDS, player.getUpcards(), overlap, true, padding);
            hand.layoutHand(CardLocation.HAND, player.getHand(), overlap * 2 + padding, true, padding);
        } else {
            int otherPlayerCount = playerCount - 1;
            if (otherPlayerCount > 0) {
                double angle = Math.PI + (Math.PI * position / (otherPlayerCount + 1));
                int x = centerX + (int) (radiusX * Math.cos(angle));
                int y = centerY + (int) (radiusY * Math.sin(angle));
                hand.setPosition(x, y);
                hand.layoutHand(CardLocation.DOWN_CARDS, player.getDowncards(), 0, false, padding);
                hand.layoutHand(CardLocation.UP_CARDS, player.getUpcards(), overlap, true, padding);
                hand.layoutHand(CardLocation.HAND, player.getHand(), overlap * 2, false, padding);
            }
        }
    }

    private List<UICard> getSelectedCards() {
        List<UICard> selectedCards = new ArrayList<>();
        for (PlayerHand hand : playerHands.values()) {
            for (UICard card : hand.getUiCards()) {
                if (card.isSelected()) {
                    selectedCards.add(card);
                }
            }
        }
        for (UICard card : wastePile.getUiCards()) {
            if (card.isSelected()) {
                selectedCards.add(card);
            }
        }
        return selectedCards;
    }

    @Override
    public void processMouseEvent(int type, int x, int y, KeyEvent buttons) {
        if (type == DesktopPane.RELEASED) {
            for (PlayerHand hand : playerHands.values()) {
                for (int i = hand.getUiCards().size() - 1; i >= 0; i--) {
                    UICard uiCard = hand.getUiCards().get(i);
                    if (uiCard.contains(x, y)) {
                        uiCard.toggleSelection();
                        repaint();
                        return;
                    }
                }
            }

            for (int i = wastePile.getUiCards().size() - 1; i >= 0; i--) {
                UICard uiCard = wastePile.getUiCards().get(i);
                if (uiCard.contains(x, y)) {
                    uiCard.toggleSelection();
                    repaint();
                    return;
                }
            }
        }
    }
}
