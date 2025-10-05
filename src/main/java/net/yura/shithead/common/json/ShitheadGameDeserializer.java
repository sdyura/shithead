package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ShitheadGameDeserializer extends StdDeserializer<ShitheadGame> {

    private static final Map<Character, Rank> RANK_MAP = new HashMap<>();
    private static final Map<Character, Suit> SUIT_MAP = new HashMap<>();

    static {
        for (Rank r : Rank.THIRTEEN_RANKS) {
            RANK_MAP.put(r.toChar(), r);
        }
        for (Suit s : Suit.FOUR_SUITS) {
            SUIT_MAP.put(s.toChar(), s);
        }
    }

    public ShitheadGameDeserializer() {
        this(null);
    }

    public ShitheadGameDeserializer(Class<?> vc) {
        super(vc);
    }

    private Card cardFromString(String s) {
        if (s == null || s.length() != 2) {
            throw new IllegalArgumentException("Invalid card string: " + s);
        }
        Rank rank = RANK_MAP.get(s.charAt(0));
        Suit suit = SUIT_MAP.get(s.charAt(1));
        if (rank == null || suit == null) {
            throw new IllegalArgumentException("Invalid card string: " + s);
        }
        return Card.getCardByRankSuit(rank, suit);
    }

    private List<Card> deserializeCardsArray(JsonParser p) throws IOException {
        List<Card> cards = new ArrayList<>();
        while (p.nextToken() != JsonToken.END_ARRAY) {
            cards.add(cardFromString(p.getText()));
        }
        return cards;
    }

    @Override
    public ShitheadGame deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String currentPlayerName = null;
        List<Card> deckCardsList = new ArrayList<>();
        List<Card> wastePileCards = new ArrayList<>();
        List<Player> tempPlayers = new ArrayList<>();

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.getCurrentName();
            p.nextToken();

            if ("currentPlayerName".equals(fieldName)) {
                currentPlayerName = p.getText();
            } else if ("deck".equals(fieldName)) {
                deckCardsList = deserializeCardsArray(p);
            } else if ("wastePile".equals(fieldName)) {
                wastePileCards = deserializeCardsArray(p);
            } else if ("players".equals(fieldName)) {
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    Player tempPlayer = null;
                    String name = null;
                    List<Card> hand = new ArrayList<>();
                    List<Card> upcards = new ArrayList<>();
                    List<Card> downcards = new ArrayList<>();
                    while (p.nextToken() != JsonToken.END_OBJECT) {
                        String playerField = p.getCurrentName();
                        p.nextToken();
                        if ("name".equals(playerField)) {
                            name = p.getText();
                        } else if ("hand".equals(playerField)) {
                            hand = deserializeCardsArray(p);
                        } else if ("upcards".equals(playerField)) {
                            upcards = deserializeCardsArray(p);
                        } else if ("downcards".equals(playerField)) {
                            downcards = deserializeCardsArray(p);
                        } else {
                            p.skipChildren();
                        }
                    }
                    tempPlayer = new Player(name);
                    tempPlayer.getHand().addAll(hand);
                    tempPlayer.getUpcards().addAll(upcards);
                    tempPlayer.getDowncards().addAll(downcards);
                    tempPlayers.add(tempPlayer);
                }
            } else {
                p.skipChildren();
            }
        }

        Stack<Card> deckCardsStack = new Stack<>();
        deckCardsStack.addAll(deckCardsList);
        Deck deck = new Deck(0);
        // The Deck class from the external cardsengine.jar library does not provide a
        // public method to set its internal card collection. Reflection is used here
        // as a workaround to inject the deserialized list of cards into the Deck.
        try {
            Field cardsField = Deck.class.getDeclaredField("cards");
            cardsField.setAccessible(true);
            cardsField.set(deck, deckCardsStack);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set deck cards via reflection", e);
        }

        List<String> playerNames = new ArrayList<>();
        for (Player tempPlayer : tempPlayers) {
            playerNames.add(tempPlayer.getName());
        }
        ShitheadGame game = new ShitheadGame(playerNames, deck);

        for (Player gamePlayer : game.getPlayers()) {
            for (Player tempPlayer : tempPlayers) {
                if (gamePlayer.getName().equals(tempPlayer.getName())) {
                    gamePlayer.getHand().addAll(tempPlayer.getHand());
                    gamePlayer.getUpcards().addAll(tempPlayer.getUpcards());
                    gamePlayer.getDowncards().addAll(tempPlayer.getDowncards());
                    break;
                }
            }
        }

        game.setWastePile(wastePileCards);

        int currentPlayerIndex = -1;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).getName().equals(currentPlayerName)) {
                currentPlayerIndex = i;
                break;
            }
        }
        if (currentPlayerIndex != -1) {
            game.setCurrentPlayer(currentPlayerIndex);
        }

        return game;
    }
}