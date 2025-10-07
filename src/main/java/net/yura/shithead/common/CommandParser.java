package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandParser {

    public Card parse(ShitheadGame game, String command) throws InvalidCommandException {

        String[] tokens = command.toLowerCase(Locale.ROOT).split(" ");
        if (tokens.length == 0) {
            throw new InvalidCommandException("empty command");
        }

        Player player = game.getCurrentPlayer();

        switch (tokens[0]) {
            case "play":
                if (tokens.length < 3) {
                    throw new InvalidCommandException("incomplete play command");
                }
                List<Card> cards;
                Card revealed = null;
                switch (tokens[1]) {
                    case "hand":
                        List<Card> cardsToPlay = new ArrayList<>();
                        for (int i = 2; i < tokens.length; i++) {
                            Card card = cardFromString(tokens[i]);
                            if (player.getHand().contains(card)) {
                                cardsToPlay.add(card);
                            }
                            else {
                                throw new InvalidCommandException("card not in hand: " + tokens[i]);
                            }
                        }
                        cards = cardsToPlay;
                        break;
                    case "up":
                        List<Card> upCardsToPlay = new ArrayList<>();
                        for (int i = 2; i < tokens.length; i++) {
                            Card card = cardFromString(tokens[i]);
                            if (player.getUpcards().contains(card)) {
                                upCardsToPlay.add(card);
                            }
                            else {
                                throw new InvalidCommandException("card not in upcards: " + tokens[i]);
                            }
                        }
                        cards = upCardsToPlay;
                        break;
                    case "down":
                        if (tokens.length != 3) {
                            throw new InvalidCommandException("can only play one downcard at a time");
                        }
                        try {
                            int index = Integer.parseInt(tokens[2]);
                            Card card = player.getDowncards().get(index);
                            cards = Collections.singletonList(card);
                            revealed = card;
                        }
                        catch (NumberFormatException ex) {
                            throw new InvalidCommandException("invalid index for downcard: " + tokens[2]);
                        }
                        catch (IndexOutOfBoundsException ex) {
                            throw new InvalidCommandException("downcard index out of bounds: " + tokens[2]);
                        }
                        break;
                    default:
                        throw new InvalidCommandException("invalid card source: " + tokens[1]);
                }

                if (!game.playCards(player, cards)) {
                    throw new InvalidCommandException("invalid move");
                }
                return revealed;

            case "pickup":
                game.pickUpWastePile(player);
                return null;

            default:
                throw new InvalidCommandException("unknown command: " + tokens[0]);
        }
    }

    private static Card cardFromString(String s) throws InvalidCommandException {
        if (s.length() != 2) {
            throw new InvalidCommandException("invalid card string: " + s);
        }
        char rankChar = s.charAt(0);
        char suitChar = s.charAt(1);

        Rank rank;
        switch (rankChar) {
            case 'a': rank = Rank.ACE; break;
            case '2': rank = Rank.TWO; break;
            case '3': rank = Rank.THREE; break;
            case '4': rank = Rank.FOUR; break;
            case '5': rank = Rank.FIVE; break;
            case '6': rank = Rank.SIX; break;
            case '7': rank = Rank.SEVEN; break;
            case '8': rank = Rank.EIGHT; break;
            case '9': rank = Rank.NINE; break;
            case 't': rank = Rank.TEN; break;
            case 'j': rank = Rank.JACK; break;
            case 'q': rank = Rank.QUEEN; break;
            case 'k': rank = Rank.KING; break;
            default: throw new InvalidCommandException("unknown rank: " + rankChar);
        }

        Suit suit;
        switch (suitChar) {
            case 'c': suit = Suit.CLUBS; break;
            case 'd': suit = Suit.DIAMONDS; break;
            case 'h': suit = Suit.HEARTS; break;
            case 's': suit = Suit.SPADES; break;
            default: throw new InvalidCommandException("unknown suit: " + suitChar);
        }

        return Card.getCardByRankSuit(rank, suit);
    }
}