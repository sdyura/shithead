package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoveSequenceGenerator {

    public static void main(String[] args) throws Exception {

        new MoveSequenceGenerator().generateMoveSequence();

    }

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

        while (!game.isFinished()) {
            String command = AutoPlay.getValidGameCommand(game);

            System.out.println("playing: " + command);

            commands.add(command);
            parser.parse(game, command);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/resources/hardcoded-game-sequence.txt"))) {
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
