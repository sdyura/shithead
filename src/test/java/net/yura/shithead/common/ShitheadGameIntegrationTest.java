package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class ShitheadGameIntegrationTest {

    @Test
    public void testFullGameWithStateVerificationAtEachStep() {
        // Setup for the 2-player game, now local to the test method.
        Deck deck = new Deck(1);
        deck.setRandom(new Random(2024L));
        ShitheadGame game = new ShitheadGame(2, deck);
        game.deal();

        int maxTurns = 200;
        int turn = 0;

        // --- Game Loop ---
        // The test will loop until the game is finished, simulating each player's turn.
        while (!game.isFinished() && turn < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            Player otherPlayer = getOtherPlayer(game, currentPlayer);
            int initialHandSize = currentPlayer.getHand().size();
            int initialWastePileSize = game.getWastePile().size();

            Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);

            // --- Player Logic: Find a valid card to play ---
            // The AI finds the best card from hand or up-cards.
            Card cardToPlay = findBestPlayableCard(game, currentPlayer, topCard);

            if (cardToPlay != null) {
                // --- Action: Play a valid card ---
                boolean wasBurn = cardToPlay.getRank() == net.yura.cardsengine.Rank.TEN;
                game.playCards(Collections.singletonList(cardToPlay));

                // --- Verification: Check game state after playing ---
                if (wasBurn) {
                    assertEquals(0, game.getWastePile().size(), "Waste pile should be burned.");
                    assertEquals(currentPlayer, game.getCurrentPlayer(), "Player should get another turn after a burn.");
                } else {
                    assertEquals(initialWastePileSize + 1, game.getWastePile().size(), "Waste pile should increase by one.");
                    assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance to the next player.");
                }
            } else {
                // --- Action: No playable card found ---
                if (!currentPlayer.getHand().isEmpty() || !currentPlayer.getUpcards().isEmpty()) {
                    // --- Sub-Phase: Hand or Up-cards ---
                    // If the player has cards in hand or up-cards but none are playable, they must pick up the pile.
                    game.pickUpWastePile();
                    assertEquals(0, game.getWastePile().size(), "Waste pile should be empty after pickup.");
                    assertEquals(initialHandSize + initialWastePileSize, currentPlayer.getHand().size(), "Player's hand should contain the picked-up pile.");
                    assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance after picking up pile.");
                } else {
                    // --- Sub-Phase: Down-cards ---
                    // If hand and up-cards are empty, the player must play a down-card blindly.
                    Card downCard = currentPlayer.getDowncards().get(0);
                    boolean playSuccessful = game.playCards(Collections.singletonList(downCard));

                    if (!playSuccessful) {
                        // --- Verification: Check penalty for invalid down-card ---
                        assertEquals(0, game.getWastePile().size(), "Waste pile should be empty after penalty pickup.");
                        assertEquals(initialWastePileSize + 1, currentPlayer.getHand().size(), "Player's hand should contain the pile and the invalid down card.");
                        assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance after penalty.");
                    }
                }
            }
            turn++;
        }

        // --- Final Verification ---
        assertTrue(turn < maxTurns, "Game did not finish within the turn limit, possible infinite loop.");
        assertTrue(game.isFinished(), "Game should be finished.");
        assertEquals(1, game.getPlayers().size(), "There should be one loser left.");
    }

    private Player getOtherPlayer(ShitheadGame game, Player currentPlayer) {
        for (Player p : game.getPlayers()) {
            if (p != currentPlayer) {
                return p;
            }
        }
        return null; // Should not happen in a 2-player game
    }

    private Card findBestPlayableCard(ShitheadGame game, Player player, Card topCard) {
        Card bestCard = null;
        List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

        for (Card card : source) {
            if (game.isPlayable(card.getRank(), topCard)) {
                if (bestCard == null || card.getRank().toInt() < bestCard.getRank().toInt()) {
                    bestCard = card;
                }
            }
        }
        return bestCard;
    }

    @Test
    public void testFull3PlayerGameWithRuleBasedPlayer() {
        // This test has its own setup for a 3-player game.
        Deck deck = new Deck(1);
        deck.setRandom(new Random(2025L));
        ShitheadGame game = new ShitheadGame(3, deck);
        game.deal();

        int maxTurns = 300;
        int turn = 0;

        // --- Game Loop (3-Player) ---
        while (!game.isFinished() && turn < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            int initialHandSize = currentPlayer.getHand().size();
            int initialWastePileSize = game.getWastePile().size();

            Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);

            // --- Player Logic: Find a valid card to play ---
            Card cardToPlay = findBestPlayableCard(game, currentPlayer, topCard);

            if (cardToPlay != null) {
                // --- Action: Play a valid card ---
                Player playerBeforeMove = currentPlayer;
                game.playCards(Collections.singletonList(cardToPlay));
                Player playerAfterMove = game.getCurrentPlayer();

                // --- Verification: Check game state after playing ---
                if (playerAfterMove == playerBeforeMove) {
                    // Player got another turn, which means the pile was burned.
                    assertEquals(0, game.getWastePile().size(), "3P: Waste pile should be empty if player gets another turn.");
                } else {
                    // Turn advanced normally.
                    assertEquals(initialWastePileSize + 1, game.getWastePile().size(), "3P: Waste pile should increase by one if turn advances.");
                }
            } else {
                // --- Action: No playable card found ---
                if (!currentPlayer.getHand().isEmpty() || !currentPlayer.getUpcards().isEmpty()) {
                    // --- Sub-Phase: Hand or Up-cards ---
                    // Player must pick up the pile if they have no valid moves from hand or up-cards.
                    game.pickUpWastePile();
                    assertEquals(0, game.getWastePile().size(), "3P: Waste pile should be empty after pickup.");
                    assertEquals(initialHandSize + initialWastePileSize, currentPlayer.getHand().size(), "3P: Player's hand should contain the picked-up pile.");
                    assertNotEquals(currentPlayer, game.getCurrentPlayer(), "3P: Turn should advance after picking up pile.");
                } else {
                    // --- Sub-Phase: Down-cards ---
                    // Player must play a down-card blindly.
                    Card downCard = currentPlayer.getDowncards().get(0);
                    boolean playSuccessful = game.playCards(Collections.singletonList(downCard));

                    if (!playSuccessful) {
                        // --- Verification: Check penalty for invalid down-card ---
                        assertEquals(0, game.getWastePile().size(), "3P: Waste pile should be empty after penalty pickup.");
                        assertEquals(initialWastePileSize + 1, currentPlayer.getHand().size(), "3P: Player's hand should contain the pile and the invalid down card.");
                    }
                }
            }
            turn++;
        }

        // --- Final Verification ---
        assertTrue(turn < maxTurns, "Game did not finish within the turn limit for 3 players.");
        assertTrue(game.isFinished(), "3-player game should be finished.");
        assertEquals(1, game.getPlayers().size(), "There should be one loser left in a 3-player game.");
    }
}