package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.shithead.common.json.SerializerUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandParser {

    public Card parse(ShitheadGame game, String command) {
        if (command.startsWith("play down ")) {
            int index = Integer.parseInt(command.substring("play down ".length()));
            Player player = game.getCurrentPlayer();
            Card card = player.getDowncards().get(index);
            execute(game, "play down " + card);
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
                if (tokens.length != 4) {
                    throw new IllegalArgumentException("incomplete rearrange command");
                }
                try {
                    String playerName = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8.name());
                    Player player = game.getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().orElse(null);
                    if (player == null) {
                        throw new IllegalArgumentException("player not found: " + playerName);
                    }
                    Card handCard = SerializerUtil.cardFromString(tokens[2]);
                    Card upCard = SerializerUtil.cardFromString(tokens[3]);
                    game.rearrangeCards(player, handCard, upCard);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return;
            case "ready":
                if (tokens.length != 2) {
                    throw new IllegalArgumentException("incomplete ready command");
                }
                try {
                    String playerName = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8.name());
                    Player player = game.getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().orElse(null);
                    if (player == null) {
                        throw new IllegalArgumentException("player not found: " + playerName);
                    }
                    game.playerReady(player);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return;
            case "play":
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
                game.pickUpWastePile();
                return;

            case "rename":
                if (tokens.length != 3) {
                    throw new IllegalArgumentException("incomplete rename command");
                }
                try {
                    String oldName = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8.name());
                    String newName = URLDecoder.decode(tokens[2], StandardCharsets.UTF_8.name());
                    game.renamePlayer(oldName, newName);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return;

            default:
                throw new IllegalArgumentException("unknown command: " + tokens[0]);
        }
    }
}