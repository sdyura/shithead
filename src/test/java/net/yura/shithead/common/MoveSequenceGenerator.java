package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoveSequenceGenerator {

    @Test
    public void generateMoveSequence() throws IOException {
        CommandParser parser = new CommandParser();
        ShitheadGame game = new ShitheadGame(2);
        Deck deck = game.getDeck();
        deck.setRandom(new Random(123)); // Same seed as the failing test
        game.deal();

        parser.parse(game, "ready Player+1");
        parser.parse(game, "ready Player+2");

        List<String> commands = new ArrayList<>();
        commands.add("ready Player+1");
        commands.add("ready Player+2");

        int maxTurns = 500;
        int turn = 0;

        while (!game.isFinished() && turn < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            Card cardToPlay = AutoPlay.findBestVisibleCard(game);

            String command;
            if (cardToPlay != null) {
                command = "play " + getCardLocation(currentPlayer, cardToPlay) + " " + cardToPlay.toString();
            } else {
                if (!currentPlayer.getHand().isEmpty() || !currentPlayer.getUpcards().isEmpty()) {
                    command = "pickup";
                } else {
                    command = "play down 0";
                }
            }
            commands.add(command);
            parser.parse(game, command);
            turn++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/resources/net/yura/shithead/common/hardcoded-game-sequence.txt"))) {
            for (String command : commands) {
                writer.write(command);
                writer.newLine();
            }
        }
    }

    private String getCardLocation(Player player, Card card) {
        if (player.getHand().contains(card)) {
            return "hand";
        } else if (player.getUpcards().contains(card)) {
            return "up";
        } else {
            throw new IllegalStateException("Card not found in hand or upcards");
        }
    }
}
