package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class ShitheadGameIntegrationTest {

    private ShitheadGame game;

    @BeforeEach
    void setUp() {
        Deck deck = new Deck(1);
        // Using a new seed to ensure a fresh, deterministic game.
        deck.setRandom(new Random(2024L));
        game = new ShitheadGame(2, deck);
        game.deal();
    }

    @Test
    public void testFullGameWithStateVerificationAtEachStep() {
        int maxTurns = 200; // Safety break to prevent infinite loops
        int turn = 0;

        while (!game.isFinished() && turn < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            Player otherPlayer = getOtherPlayer(currentPlayer);
            int initialHandSize = currentPlayer.getHand().size();
            int initialWastePileSize = game.getWastePile().size();

            Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);
            Card cardToPlay = findBestPlayableCard(currentPlayer, topCard);

            if (cardToPlay != null) {
                boolean wasBurn = cardToPlay.getRank() == net.yura.cardsengine.Rank.TEN;
                game.playCards(currentPlayer, Collections.singletonList(cardToPlay));

                if (wasBurn) {
                    assertEquals(0, game.getWastePile().size(), "Waste pile should be burned.");
                    assertEquals(currentPlayer, game.getCurrentPlayer(), "Player should get another turn after a burn.");
                } else {
                    assertEquals(initialWastePileSize + 1, game.getWastePile().size(), "Waste pile should increase by one.");
                    assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance to the next player.");
                }
            } else {
                // No playable cards from hand or up-cards
                if (!currentPlayer.getHand().isEmpty() || !currentPlayer.getUpcards().isEmpty()) {
                    game.pickUpWastePile(currentPlayer);
                    assertEquals(0, game.getWastePile().size(), "Waste pile should be empty after pickup.");
                    assertEquals(initialHandSize + initialWastePileSize, currentPlayer.getHand().size(), "Player's hand should contain the picked-up pile.");
                    assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance after picking up pile.");
                } else {
                    // Must play from down-cards
                    Card downCard = currentPlayer.getDowncards().get(0);
                    boolean playSuccessful = game.playCards(currentPlayer, Collections.singletonList(downCard));

                    if (!playSuccessful) {
                        // Penalty for invalid down-card
                        assertEquals(0, game.getWastePile().size(), "Waste pile should be empty after penalty pickup.");
                        // Hand now contains the pile + the invalid card.
                        assertEquals(initialWastePileSize + 1, currentPlayer.getHand().size(), "Player's hand should contain the pile and the invalid down card.");
                        assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance after penalty.");
                    }
                }
            }
            turn++;
        }

        assertTrue(turn < maxTurns, "Game did not finish within the turn limit, possible infinite loop.");
        assertTrue(game.isFinished(), "Game should be finished.");
        assertEquals(1, game.getPlayers().size(), "There should be one loser left.");
    }

    private Player getOtherPlayer(Player currentPlayer) {
        for (Player p : game.getPlayers()) {
            if (p != currentPlayer) {
                return p;
            }
        }
        return null; // Should not happen in a 2-player game
    }

    private Card findBestPlayableCard(Player player, Card topCard) {
        Card bestCard = null;
        // The source of cards is hand first, then up-cards. Down-cards are handled separately.
        List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

        for (Card card : source) {
            if (game.isPlayable(card.getRank(), topCard)) {
                // Simple AI: play the lowest possible valid card.
                if (bestCard == null || card.getRank().toInt() < bestCard.getRank().toInt()) {
                    bestCard = card;
                }
            }
        }
        return bestCard;
    }
}