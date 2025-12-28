package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.yura.cardsengine.Card;

public class GameView extends Panel {

    private GameViewListener gameCommandListener;

    private ShitheadGame game;
    private String myUsername;
    private final List<UICard> uiCards = new ArrayList<UICard>();
    private final Map<Player, PlayerHand> playerHands = new HashMap<Player, PlayerHand>();
    private final int padding = XULLoader.adjustSizeToDensity(2);

    public GameView() {
    }

    public void setGameCommandListener(GameViewListener gameCommandListener) {
        this.gameCommandListener = gameCommandListener;
    }

    public void setGame(ShitheadGame game, String playerID) {
        this.game = game;
        this.myUsername = playerID;
        layoutCards();
        repaint();
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
            g.translate(hand.x, hand.y);
            hand.paint(g, this);
            g.translate(-hand.x, -hand.y);
        }

        if (game.isFinished()) {
            String text = "Game Over!";
            g.setColor(0xFF000000);
            g.drawString(text, (getWidth() - g.getFont().getWidth(text)) / 2, getHeight() / 2 - CardImageManager.cardHeight / 2 - g.getFont().getHeight());
        }

        // paint deck and waste pile
        for (int i = 0; i < uiCards.size(); i++) {
            uiCards.get(i).paint(g, this);
        }
    }

    private void layoutCards() {
        uiCards.clear();
        playerHands.clear();
        if (game == null) {
            return;
        }

        List<Player> players = game.getPlayers();
        int localPlayerIndex = -1;
        if (myUsername != null) {
            for (int i = 0; i < players.size(); i++) {
                if (myUsername.equals(players.get(i).getName())) {
                    localPlayerIndex = i;
                    break;
                }
            }
        }

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int playerPosition = (i - localPlayerIndex + players.size()) % players.size();
            layoutPlayer(player, playerPosition, i == localPlayerIndex);
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
                uiCards.add(deckCard);
            }
        }

        // Waste Pile
        int dip = XULLoader.adjustSizeToDensity(1);
        List<Card> wastePile = game.getWastePile();
        int wastePileSize = wastePile.size();
        if (wastePileSize > 0) {
            int stackHeightWaste = CardImageManager.cardHeight + (Math.min(wastePileSize, 3) - 1) * padding;
            int yStartWaste = centerY - stackHeightWaste / 2;
            for (int i = 0; i < wastePileSize; i++) {
                Card card = wastePile.get(i);
                UICard wastePileCard = new UICard(card, null, CardLocation.WASTE, true);
                wastePileCard.setPosition(centerX + padding / 2, yStartWaste);
                uiCards.add(wastePileCard);
                if (i < (wastePileSize - 3)) {
                    yStartWaste = yStartWaste + dip;
                }
                else {
                    if ((i == (wastePileSize - 3) && card.getRank() == wastePile.get(wastePileSize - 1).getRank() && wastePile.get(wastePileSize - 2).getRank() == wastePile.get(wastePileSize - 1).getRank()) ||
                        (i == (wastePileSize - 2) && card.getRank() == wastePile.get(wastePileSize - 1).getRank())) {
                        yStartWaste = yStartWaste + padding * 4;
                    }
                    else {
                        yStartWaste = yStartWaste + padding;
                    }
                }
            }
        }
    }

    private void layoutPlayer(Player player, int position, boolean isLocalPlayer) {
        int playerCount = game.getPlayers().size();
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radiusX = width / 2 - 50;
        int radiusY = height / 2 - 50;
        int overlap = XULLoader.adjustSizeToDensity(20);

        PlayerHand hand = new PlayerHand(game, player, isLocalPlayer, gameCommandListener);
        playerHands.put(player, hand);

        if (game.isRearranging()) {
            if (!game.getPlayersReady().contains(player)) {
                hand.setWaitingForInput(true);
            }
        }
        else if (game.getCurrentPlayer() == player) {
            hand.setWaitingForInput(true);
        }

        if (isLocalPlayer) {
            hand.setPosition(centerX, height - CardImageManager.cardHeight - padding - overlap * 2);
            hand.layoutHand(CardLocation.DOWN_CARDS, player.getDowncards(), 0, false);
            hand.layoutHand(CardLocation.UP_CARDS, player.getUpcards(), overlap, true);
            hand.layoutHand(CardLocation.HAND, player.getHand(), overlap * 2 + padding, true);
        } else {
            int otherPlayerCount = playerCount - 1;
            if (otherPlayerCount > 0) {
                double angle = Math.PI + (Math.PI * position / (otherPlayerCount + 1));
                int x = centerX + (int) (radiusX * Math.cos(angle));
                int y = centerY + (int) (radiusY * Math.sin(angle));
                hand.setPosition(x, y);
                hand.layoutHand(CardLocation.DOWN_CARDS, player.getDowncards(), 0, false);
                hand.layoutHand(CardLocation.UP_CARDS, player.getUpcards(), overlap, true);
                hand.layoutHand(CardLocation.HAND, player.getHand(), overlap * 2, false);
            }
        }
    }

    @Override
    public void processMouseEvent(int type, int x, int y, KeyEvent buttons) {

        if (type == DesktopPane.RELEASED) {
            for (int i = uiCards.size() - 1; i >= 0; i--) {
                UICard uiCard = uiCards.get(i);
                // if user clicks on waste pile during our turn, this mean we should pick up the waste pile
                if (uiCard.contains(x, y) && game.getCurrentPlayer().getName().equals(myUsername)) {
                    if (uiCard.getLocation() == CardLocation.WASTE) {
                        gameCommandListener.pickUpWaste();
                    }
                    else if (uiCard.getLocation() == CardLocation.DECK) {
                        gameCommandListener.playDeck();
                    }
                }
            }
            for (PlayerHand hand : playerHands.values()) {
                // only allow clicking on my own cards
                if (hand.player.getName().equals(myUsername) && hand.isWaitingForInput()) {
                    if (hand.processMouseEvent(type, x - hand.x, y - hand.y, buttons)) {
                        repaint();
                        return;
                    }
                }
            }
        }
    }
}
