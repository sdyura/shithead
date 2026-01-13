package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Animation;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.yura.cardsengine.Card;
import org.apache.commons.collections4.iterators.ReverseListIterator;

public class GameView extends Panel {

    private GameViewListener gameCommandListener;

    private ShitheadGame game;
    private String myUsername;
    private final List<UICard> deckAndWasteUICards = new ArrayList<UICard>();
    private final Map<Card, UICard> cardToUICard = new HashMap<>();
    private final Map<Player, PlayerHand> playerHands = new HashMap<Player, PlayerHand>();
    private final int padding = XULLoader.adjustSizeToDensity(2);

    public GameView() {
        Animation.FPS = 30;
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

    private PlayerHand getPlayerHand(String username) {
        return playerHands.values().stream().filter(p -> p.player.getName().equals(username)).findAny().orElse(null);
    }

    public List<Card> getSelectedCards() {
        PlayerHand hand = getPlayerHand(myUsername);
        if (hand == null) {
            return Collections.emptyList();
        }
        // TODO maybe use hand.getSelectedCards
        return hand.getUiCards().stream().filter(UICard::isSelected).map(UICard::getCard).collect(Collectors.toList());
    }

    public void clearSelectedCards() {
        PlayerHand hand = getPlayerHand(myUsername);
        if (hand != null) {
            hand.getUiCards().forEach(c -> c.setSelected(false));
        }
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

        if (game.isFinished()) {
            String text = "Game Over!";
            g.setColor(0xFF000000);
            g.drawString(text, (getWidth() - g.getFont().getWidth(text)) / 2, getHeight() / 2 - CardImageManager.cardHeight / 2 - g.getFont().getHeight());
        }

        // paint deck and waste pile
        for (int i = 0; i < deckAndWasteUICards.size(); i++) {
            deckAndWasteUICards.get(i).paint(g, this);
        }
    }

    private void layoutCards() {

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

        List<UICard> wasteLeftover = deckAndWasteUICards.stream().filter(c -> c.getLocation() == CardLocation.WASTE).filter(c -> !game.getWastePile().contains(c.getCard())).collect(Collectors.toList());
        wasteLeftover.forEach(deckAndWasteUICards::remove);

        List<UICard> playerLeftOver = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int playerPosition = (i - localPlayerIndex + players.size()) % players.size();
            playerLeftOver.addAll(layoutPlayer(wasteLeftover, player, playerPosition, i == localPlayerIndex));
        }
        playerHands.entrySet().removeIf(h -> {
            boolean remove = !players.contains(h.getKey());
            playerLeftOver.addAll(h.getValue().getUiCards());
            return remove;
        });


        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Waste Pile
        int dip = XULLoader.adjustSizeToDensity(1);
        List<Card> wastePile = game.getWastePile();
        int wastePileSize = wastePile.size();
        if (wastePileSize > 0) {
            int stackHeightWaste = CardImageManager.cardHeight + (Math.min(wastePileSize, 3) - 1) * padding;
            int yStartWaste = centerY - stackHeightWaste / 2;
            for (int i = 0; i < wastePileSize; i++) {
                Card card = wastePile.get(i);
                UICard wastePileCard = getUICard(playerLeftOver, card, CardLocation.WASTE, true);
                wastePileCard.setPosition(centerX + padding / 2, yStartWaste);
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

        // Deck
        int deckSize = game.getDeck().getCards().size();
        int cardsToShowDeck = Math.min(deckSize, 3);

        List<UICard> deckUICards = deckAndWasteUICards.stream().filter(c -> c.getLocation() == CardLocation.DECK).collect(Collectors.toList());

        if (deckUICards.size() > cardsToShowDeck) {
            throw new IllegalStateException("too many deck cards");
        }

        if (cardsToShowDeck > 0) {
            int stackHeightDeck = CardImageManager.cardHeight + (cardsToShowDeck - 1) * padding;
            int yStartDeck = centerY - stackHeightDeck / 2;

            // create deck cards
            while (deckUICards.size() < cardsToShowDeck) {
                UICard newlyVisibleDeckCard = new UICard(null, CardLocation.DECK, false,
                        centerX - CardImageManager.cardWidth - padding / 2, yStartDeck);
                deckUICards.add(newlyVisibleDeckCard);
                deckAndWasteUICards.add(newlyVisibleDeckCard);
            }

            for (int i = 0; i < cardsToShowDeck; i++) {
                UICard deckCard = deckUICards.get(i);
                deckCard.setPosition(centerX - CardImageManager.cardWidth - padding / 2, yStartDeck + i * padding);
            }
        }

        Animation.registerAnimated(this);
    }

    private List<UICard> layoutPlayer(List<UICard> wasteUICards, Player player, int position, boolean isLocalPlayer) {
        int playerCount = game.getPlayers().size();
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radiusX = width / 2 - 50;
        int radiusY = height / 2 - 50;
        int overlap = XULLoader.adjustSizeToDensity(20);

        PlayerHand hand = playerHands.get(player);
        if (hand == null) {
            hand = new PlayerHand(game, player, isLocalPlayer, gameCommandListener);
            playerHands.put(player, hand);
        }

        if (game.isRearranging()) {
            hand.setWaitingForInput(!game.getPlayersReady().contains(player));
        }
        else {
            hand.setWaitingForInput(game.getCurrentPlayer() == player);
        }

        Card top = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);

        List<UICard> downUiCards = player.getDowncards().stream()
                .map(card -> getUICard(wasteUICards, card, CardLocation.DOWN_CARDS, false))
                .collect(Collectors.toList());

        List<UICard> upUiCards = player.getUpcards().stream()
                .map(card -> getUICard(wasteUICards, card, CardLocation.UP_CARDS, true))
                .collect(Collectors.toList());

        List<UICard> handUiCards = player.getHand().stream()
                .map(card -> getUICard(wasteUICards, card, CardLocation.HAND, isLocalPlayer))
                .collect(Collectors.toList());

        if (isLocalPlayer && hand.isWaitingForInput()) {
            boolean handActive = !player.getHand().isEmpty();
            boolean upcardsActive = player.getHand().isEmpty() && !player.getUpcards().isEmpty();

            if (handActive) {
                handUiCards.forEach(c -> c.setPlayable(game.isPlayable(c.getCard().getRank(), top)));
            }
            if (upcardsActive) {
                upUiCards.forEach(c -> c.setPlayable(game.isPlayable(c.getCard().getRank(), top)));
            }
        }

        List<UICard> allPlayerCards = Stream.of(downUiCards, upUiCards, handUiCards)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<UICard> leftOver = hand.setCards(allPlayerCards);
        if (isLocalPlayer) {
            hand.setPosition(centerX, height - CardImageManager.cardHeight - padding - overlap * 2);
            hand.layoutHand(downUiCards, 0);
            hand.layoutHand(upUiCards, overlap);
            hand.layoutHand(handUiCards, overlap * 2 + padding);
        } else {
            double angle = Math.PI + (Math.PI * position / playerCount);
            int x = centerX + (int) (radiusX * Math.cos(angle));
            int y = centerY + (int) (radiusY * Math.sin(angle));
            hand.setPosition(x, y);
            hand.layoutHand(downUiCards, 0);
            hand.layoutHand(upUiCards, overlap);
            hand.layoutHand(handUiCards, overlap * 2);
        }
        return leftOver;
    }

    private UICard getUICard(List<UICard> leftOver, Card card, CardLocation location, boolean isFaceUp) {
        UICard uiCard = cardToUICard.get(card);
        if (uiCard == null) {
            if (!leftOver.isEmpty()) {
                uiCard = leftOver.stream().filter(c -> c.getCard() == card).findFirst().orElseGet(
                        () -> leftOver.stream().filter(c -> c.getCard() == null).findFirst().orElse(null)
                );
                if (uiCard != null) {
                    leftOver.remove(uiCard);
                }
            }
            if (uiCard == null) {
                uiCard = StreamSupport.stream(((Iterable<UICard>) () -> new ReverseListIterator<>(deckAndWasteUICards)).spliterator(), false)
                        .filter(c -> c.getLocation() == CardLocation.DECK).findFirst().orElse(null);
                if (uiCard == null) {
                    // dealing a brand new card, this should ONLY happen at the start of the game
                    System.out.println("Dealing new card on table (ONLY AT START OF GAME)");
                    uiCard = new UICard();
                }
            }
        }
        updateCardInfo(uiCard, card, location, isFaceUp);
        return uiCard;
    }

    private void updateCardInfo(UICard uiCard, Card card, CardLocation location, boolean isFaceUp) {
        CardLocation oldLocation = uiCard.getLocation();
        if (oldLocation == CardLocation.DECK || oldLocation == CardLocation.WASTE) {
            deckAndWasteUICards.remove(uiCard);
        }
        // if we are moving from deck to waste, we WANT to remove it and add to make sure its at the top
        if (location == CardLocation.DECK || location == CardLocation.WASTE) {
            deckAndWasteUICards.add(uiCard);
        }
        if (card != uiCard.getCard()) {
            uiCard.setCard(card);
        }
        uiCard.setPlayable(false);
        uiCard.setLocation(location);
        uiCard.setFaceUp(isFaceUp);
        if (card != null) {
            cardToUICard.put(card, uiCard);
        }
    }

    @Override
    public void processMouseEvent(int type, int x, int y, KeyEvent buttons) {

        if (type == DesktopPane.RELEASED) {
            for (int i = deckAndWasteUICards.size() - 1; i >= 0; i--) {
                UICard uiCard = deckAndWasteUICards.get(i);
                // if user clicks on waste pile during our turn, this mean we should pick up the waste pile
                if (uiCard.contains(x, y) && game.getCurrentPlayer().getName().equals(myUsername)) {
                    if (uiCard.getLocation() == CardLocation.WASTE) {
                        gameCommandListener.pickUpWaste();
                        return;
                    }
                    else if (uiCard.getLocation() == CardLocation.DECK) {
                        gameCommandListener.playDeck();
                        return;
                    }
                }
            }
            for (PlayerHand hand : playerHands.values()) {
                // only allow clicking on my own cards
                if (hand.player.getName().equals(myUsername) && hand.isWaitingForInput()) {
                    if (hand.processMouseEvent(type, x, y, buttons)) {
                        repaint();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void animate() {
        boolean more = false;
        for (int i = deckAndWasteUICards.size() - 1; i >= 0; i--) {
            UICard uiCard = deckAndWasteUICards.get(i);
            if (uiCard.animate()) {
                more = true;
            }
        }
        for (PlayerHand hand : playerHands.values()) {
            List<UICard> cards = hand.getUiCards();
            for (int i = cards.size() - 1; i >= 0; i--) {
                UICard uiCard = cards.get(i);
                if (uiCard.animate()) {
                    more = true;
                }
            }
        }
        if (more) {
            repaint();
            Animation.registerAnimated(this);
        }
    }
}
