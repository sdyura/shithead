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

        while (!game.isFinished() && turn < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            Player otherPlayer = getOtherPlayer(game, currentPlayer);
            int initialHandSize = currentPlayer.getHand().size();
            int initialWastePileSize = game.getWastePile().size();

            Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);
            Card cardToPlay = findBestPlayableCard(game, currentPlayer, topCard);

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
                if (!currentPlayer.getHand().isEmpty() || !currentPlayer.getUpcards().isEmpty()) {
                    game.pickUpWastePile(currentPlayer);
                    assertEquals(0, game.getWastePile().size(), "Waste pile should be empty after pickup.");
                    assertEquals(initialHandSize + initialWastePileSize, currentPlayer.getHand().size(), "Player's hand should contain the picked-up pile.");
                    assertEquals(otherPlayer, game.getCurrentPlayer(), "Turn should advance after picking up pile.");
                } else {
                    Card downCard = currentPlayer.getDowncards().get(0);
                    boolean playSuccessful = game.playCards(currentPlayer, Collections.singletonList(downCard));

                    if (!playSuccessful) {
                        assertEquals(0, game.getWastePile().size(), "Waste pile should be empty after penalty pickup.");
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

        while (!game.isFinished() && turn < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            int initialHandSize = currentPlayer.getHand().size();
            int initialWastePileSize = game.getWastePile().size();

            Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);
            Card cardToPlay = findBestPlayableCard(game, currentPlayer, topCard);

            if (cardToPlay != null) {
                Player playerBeforeMove = currentPlayer;
                game.playCards(currentPlayer, Collections.singletonList(cardToPlay));
                Player playerAfterMove = game.getCurrentPlayer();

                if (playerAfterMove == playerBeforeMove) {
                    assertEquals(0, game.getWastePile().size(), "3P: Waste pile should be empty if player gets another turn.");
                } else {
                    assertEquals(initialWastePileSize + 1, game.getWastePile().size(), "3P: Waste pile should increase by one if turn advances.");
                }
            } else {
                if (!currentPlayer.getHand().isEmpty() || !currentPlayer.getUpcards().isEmpty()) {
                    game.pickUpWastePile(currentPlayer);
                    assertEquals(0, game.getWastePile().size(), "3P: Waste pile should be empty after pickup.");
                    assertEquals(initialHandSize + initialWastePileSize, currentPlayer.getHand().size(), "3P: Player's hand should contain the picked-up pile.");
                    assertNotEquals(currentPlayer, game.getCurrentPlayer(), "3P: Turn should advance after picking up pile.");
                } else {
                    Card downCard = currentPlayer.getDowncards().get(0);
                    boolean playSuccessful = game.playCards(currentPlayer, Collections.singletonList(downCard));

                    if (!playSuccessful) {
                        assertEquals(0, game.getWastePile().size(), "3P: Waste pile should be empty after penalty pickup.");
                        assertEquals(initialWastePileSize + 1, currentPlayer.getHand().size(), "3P: Player's hand should contain the pile and the invalid down card.");
                    }
                }
            }
            turn++;
        }

        assertTrue(turn < maxTurns, "Game did not finish within the turn limit for 3 players.");
        assertTrue(game.isFinished(), "3-player game should be finished.");
        assertEquals(1, game.getPlayers().size(), "There should be one loser left in a 3-player game.");
    }
}