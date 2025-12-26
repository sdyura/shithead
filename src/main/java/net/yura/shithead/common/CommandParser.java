package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.shithead.common.json.SerializerUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CommandParser {

    public Card parse(ShitheadGame game, String command) {
        if (command.startsWith("play down ")) {
            int index = Integer.parseInt(command.substring("play down ".length()));
            Player player = game.getCurrentPlayer();
            Card card = player.getDowncards().get(index);
            execute(game, "play down " + card);
            return card;
        }
        if (command.equals("play deck")) {
            Card card = game.getTopCardFromDeck();
            if (card != null) {
                execute(game, "play deck " + card);
            }
            return card;
        }

        execute(game, command);
        return null;
    }

    public void execute(ShitheadGame game, String command) {

        String[] tokens = command.split(" ");
        if (tokens.length == 0) {
            throw new IllegalArgumentException("empty command");
        }

        switch (tokens[0]) {
            case "rearrange":
                if (!game.isRearranging()) {
                    throw new IllegalArgumentException("game not in rearranging mode");
                }
                if (tokens.length != 4) {
                    throw new IllegalArgumentException("incomplete rearrange command");
                }
                String playerName = decodePlayerName(tokens[1]);
                Player player = game.getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().orElse(null);
                if (player == null) {
                    throw new IllegalArgumentException("player not found: " + playerName);
                }
                Card handCard = SerializerUtil.cardFromString(tokens[2]);
                Card upCard = SerializerUtil.cardFromString(tokens[3]);
                game.rearrangeCards(player, handCard, upCard);
                return;
            case "ready":
                if (!game.isRearranging()) {
                    throw new IllegalArgumentException("game not in rearranging mode");
                }
                if (tokens.length != 2) {
                    throw new IllegalArgumentException("incomplete ready command");
                }
                playerName = decodePlayerName(tokens[1]);
                player = game.getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().orElse(null);
                if (player == null) {
                    throw new IllegalArgumentException("player not found: " + playerName);
                }

                game.playerReady(player);
                return;
            case "play":
                if (!game.isPlaying()) {
                    throw new IllegalArgumentException("game not in playing mode");
                }
                if (tokens.length < 3) {
                    throw new IllegalArgumentException("incomplete play command");
                }
                List<Card> cards;
                boolean isDowncardPlay = false;
                switch (tokens[1]) {
                    case "hand":
                        List<Card> cardsToPlay = new ArrayList<>();
                        for (int i = 2; i < tokens.length; i++) {
                            cardsToPlay.add(SerializerUtil.cardFromString(tokens[i]));
                        }
                        cards = cardsToPlay;
                        break;
                    case "up":
                        List<Card> upCardsToPlay = new ArrayList<>();
                        for (int i = 2; i < tokens.length; i++) {
                            upCardsToPlay.add(SerializerUtil.cardFromString(tokens[i]));
                        }
                        cards = upCardsToPlay;
                        break;
                    case "down":
                        isDowncardPlay = true;
                        if (tokens.length != 3) {
                            throw new IllegalArgumentException("can only play one downcard at a time");
                        }
                        Card card = SerializerUtil.cardFromString(tokens[2]);
                        cards = Collections.singletonList(card);
                        break;
                    case "deck":
                        if (tokens.length != 3) {
                            throw new IllegalArgumentException("incomplete play deck command");
                        }
                        game.playCardFromDeck(SerializerUtil.cardFromString(tokens[2]));
                        return;
                    default:
                        throw new IllegalArgumentException("invalid card source: " + tokens[1]);
                }

                if (!game.playCards(cards)) {
                    if (isDowncardPlay) {
                        // A failed downcard play is a valid game event, not an error.
                        // The penalty is handled by the game logic.
                    } else {
                        throw new IllegalArgumentException("invalid move");
                    }
                }
                return;

            case "pickup":
                if (!game.isPlaying()) {
                    throw new IllegalArgumentException("game not in playing mode");
                }
                game.pickUpWastePile();
                return;

            case "rename":
                if (tokens.length != 3) {
                    throw new IllegalArgumentException("incomplete rename command");
                }
                String oldName = decodePlayerName(tokens[1]);
                String newName = decodePlayerName(tokens[2]);
                game.renamePlayer(oldName, newName);
                return;

            default:
                throw new IllegalArgumentException("unknown command: " + tokens[0]);
        }
    }

    private static String decodePlayerName(String encoded) {
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
