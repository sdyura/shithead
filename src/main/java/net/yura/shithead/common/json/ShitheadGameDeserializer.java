package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Stack;

public class ShitheadGameDeserializer extends StdDeserializer<ShitheadGame> {

    public ShitheadGameDeserializer() {
        this(null);
    }

    protected ShitheadGameDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ShitheadGame deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);

        // Let Jackson deserialize the nested objects and arrays using the registered deserializers
        List<Player> players = mapper.convertValue(node.get("players"), new TypeReference<List<Player>>() {});
        List<Card> deckCards = mapper.convertValue(node.get("deck"), new TypeReference<List<Card>>() {});
        List<Card> wastePile = mapper.convertValue(node.get("wastePile"), new TypeReference<List<Card>>() {});
        String currentPlayerName = node.get("currentPlayerName").asText();

        // The Deck class from the external cardsengine.jar library does not provide a
        // public method to set its internal card collection. Reflection is used here
        // as a workaround to inject the deserialized list of cards into the Deck.
        Stack<Card> deckStack = new Stack<>();
        deckStack.addAll(deckCards);
        Deck deck = new Deck(0); // Create an empty deck
        try {
            Field cardsField = Deck.class.getDeclaredField("cards");
            cardsField.setAccessible(true);
            cardsField.set(deck, deckStack);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set deck cards via reflection", e);
        }

        // Create the game object and populate it using the setters
        ShitheadGame game = new ShitheadGame(players.size(), deck);
        game.setPlayers(players);
        game.setWastePile(wastePile);

        // Set the current player index
        int currentPlayerIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(currentPlayerName)) {
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