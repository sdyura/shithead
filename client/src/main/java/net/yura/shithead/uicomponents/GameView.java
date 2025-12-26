package net.yura.shithead.uicomponents;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.gui.plaf.LookAndFeel;
import net.yura.mobile.logging.Logger;
import net.yura.shithead.ShitheadActionListener;
import net.yura.shithead.ShitheadUtil;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.yura.cardsengine.Card;
import net.yura.mobile.gui.ActionListener;

public class GameView extends Panel implements ActionListener {
    private ShitheadGame game;
    private String myUsername;
    private final List<UICard> uiCards = new ArrayList<UICard>();
    private final int padding = XULLoader.adjustSizeToDensity(2);
    private Button playButton;
    private ShitheadActionListener actionListener;

    public GameView() {
    }

    public void setup(XULLoader loader, ShitheadActionListener actionListener) {
        this.actionListener = actionListener;
        setName("GameView");
        playButton = (Button) loader.find("play_button");
        playButton.addActionListener(this);
    }

    public void setGame(ShitheadGame game) {
        this.game = game;
        if (game != null) {
            ShitheadUtil.sortCards(game.getCurrentPlayer().getHand());
        }
        layoutCards();
        updatePlayButton();
        repaint();
    }

    public void setPlayerID(String playerID) {
        this.myUsername = playerID;
    }

    @Override
    public void doLayout() {
        layoutCards();
    }

    @Override
    public void paintComponent(Graphics2D g) {
        super.paintComponent(g);
        for (int i = 0; i < uiCards.size(); i++) {
            uiCards.get(i).paint(g, this);
        }
    }

    private void layoutCards() {
        uiCards.clear();
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
                uiCards.add(deckCard);
            }
        }

        // Waste Pile
        List<Card> wastePile = game.getWastePile();
        int wastePileSize = wastePile.size();
        int cardsToShowWaste = Math.min(wastePileSize, 3);
        if (cardsToShowWaste > 0) {
            int stackHeightWaste = CardImageManager.cardHeight + (cardsToShowWaste - 1) * padding;
            int yStartWaste = centerY - stackHeightWaste / 2;
            for (int i = 0; i < cardsToShowWaste; i++) {
                Card card = wastePile.get(wastePileSize - cardsToShowWaste + i);
                UICard wastePileCard = new UICard(card, null, CardLocation.WASTE, true);
                wastePileCard.setPosition(centerX + padding / 2, yStartWaste + i * padding);
                uiCards.add(wastePileCard);
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

        boolean isLocalPlayer = position == 0;

        if (isLocalPlayer) {
            int x = centerX;
            int y = height - CardImageManager.cardHeight;
            layoutHand(player, CardLocation.DOWN_CARDS, player.getDowncards(), x, y - padding - overlap * 2, false, false);
            layoutHand(player, CardLocation.UP_CARDS, player.getUpcards(), x, y - padding - overlap, false, true);
            layoutHand(player, CardLocation.HAND, player.getHand(), x, y, false, true);
        } else {
            int otherPlayerCount = playerCount - 1;
            if (otherPlayerCount > 0) {
                double angle = Math.PI + (Math.PI * position / (otherPlayerCount + 1));
                int x = centerX + (int) (radiusX * Math.cos(angle));
                int y = centerY + (int) (radiusY * Math.sin(angle));
                layoutHand(player, CardLocation.DOWN_CARDS, player.getDowncards(), x, y, false, false);
                layoutHand(player, CardLocation.UP_CARDS, player.getUpcards(), x, y + overlap, false, true);
                layoutHand(player, CardLocation.HAND, player.getHand(), x, y + overlap * 2, false, false);
            }
        }
    }

    private void layoutHand(Player player, CardLocation location, List<Card> hand, int x, int y, boolean stack, boolean isFaceUp) {

        x -= ((hand.size() * CardImageManager.cardWidth) + (padding * (hand.size() - 1))) / 2;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            UICard uiCard = new UICard(card, player, location, isFaceUp);
            uiCard.setPosition(x + i * (CardImageManager.cardWidth + padding), y);
            uiCards.add(uiCard);
        }
    }
    private void updatePlayButton() {
        if (game == null) {
            playButton.setVisible(false);
            return;
        }
        playButton.setVisible(true);
        Player.State state = game.getCurrentPlayer().getState();
        List<UICard> selectedCards = getSelectedCards();
        switch (state) {
            case SWAPPING:
                playButton.setText("Ready");
                playButton.setEnabled(true);
                break;
            case PLAYING:
                if (selectedCards.isEmpty()) {
                    playButton.setText("Pickup");
                    playButton.setEnabled(true);
                } else {
                    playButton.setText("Play");
                    playButton.setEnabled(game.isPlayable(selectedCards.stream().map(UICard::getCard).collect(Collectors.toList())));
                }
                break;
            default:
                playButton.setVisible(false);
        }
    }

    private List<UICard> getSelectedCards() {
        return uiCards.stream().filter(UICard::isSelected).collect(Collectors.toList());
    }
    @Override
    public void processMouseEvent(int type, int x, int y, int modifiers) {
        super.processMouseEvent(type, x, y, modifiers);
        if (type == LookAndFeel.POINTER_RELEASED) {
            for (int i = uiCards.size() - 1; i >= 0; i--) {
                UICard uiCard = uiCards.get(i);
                if (uiCard.contains(x, y)) {
                    onCardClick(uiCard);
                    break;
                }
            }
        }
    }

    private void onCardClick(UICard uiCard) {
        if (game == null || !myUsername.equals(game.getCurrentPlayer().getName())) {
            return;
        }
        Player.State state = game.getCurrentPlayer().getState();
        if (state == Player.State.SWAPPING) {
            if (uiCard.getLocation() == CardLocation.HAND || uiCard.getLocation() == CardLocation.UP_CARDS) {
                uiCard.toggleSelection();
                List<UICard> selectedCards = getSelectedCards();
                if (selectedCards.size() == 2) {
                    UICard card1 = selectedCards.get(0);
                    UICard card2 = selectedCards.get(1);
                    actionListener.actionPerformed("swap " + card1.getCard() + " " + card2.getCard());
                    card1.setSelected(false);
                    card2.setSelected(false);
                }
            }
        } else if (state == Player.State.PLAYING) {
            if (uiCard.getPlayer() != null && uiCard.getPlayer().getName().equals(myUsername)
                    && (uiCard.getLocation() == CardLocation.HAND || uiCard.getLocation() == CardLocation.UP_CARDS)) {
                uiCard.toggleSelection();
                updatePlayButton();
            }
        }
        repaint();
    }
    @Override
    public void actionPerformed(String actionCommand) {
        if ("play".equals(actionCommand)) {
            Player.State state = game.getCurrentPlayer().getState();
            if (state == Player.State.SWAPPING) {
                actionListener.actionPerformed("ready");
            } else if (state == Player.State.PLAYING) {
                List<UICard> selectedCards = getSelectedCards();
                if (selectedCards.isEmpty()) {
                    actionListener.actionPerformed("pickup");
                } else {
                    String cardsString = selectedCards.stream()
                            .map(c -> c.getCard().toString())
                            .collect(Collectors.joining(" "));
                    actionListener.actionPerformed("play hand " + cardsString);
                }
            }
        } else {
            Logger.warn("Unknown action in GameView: " + actionCommand);
        }
    }
}
