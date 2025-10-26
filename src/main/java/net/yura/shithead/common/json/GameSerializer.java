package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.yura.cardsengine.Card;
import net.yura.cardsengine.Deck;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameSerializer extends JsonSerializer<ShitheadGame> {

    @Override
    public void serialize(ShitheadGame game, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String contextPlayerName = (String) serializers.getAttribute(PlayerSerializer.PLAYER_CONTEXT_KEY);

        gen.writeStartObject();

        if (contextPlayerName == null) {
            // Full serialization for persistence
            gen.writeObjectField("deck", getDeckCards(game));
        } else {
            // Player-specific view
            gen.writeNumberField("cardsInDeck", getDeckCards(game).size());
        }

        gen.writeObjectField("wastePile", game.getWastePile());

        gen.writeObjectField("players", game.getPlayers());
        gen.writeArrayFieldStart("playersReady");
        for (Player p : game.getPlayersReady()) {
            gen.writeString(p.getName());
        }
        gen.writeEndArray();
        gen.writeStringField("currentPlayerName", game.getCurrentPlayer().getName());
        gen.writeEndObject();
    }

    private static List<Card> getDeckCards(ShitheadGame game) {
        try {
            Field cardsField = Deck.class.getDeclaredField("cards");
            cardsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Stack<Card> cards = (Stack<Card>) cardsField.get(game.getDeck());
            return new ArrayList<>(cards);
        } catch (Exception e) {
            throw new RuntimeException("Could not get deck cards via reflection", e);
        }
    }
}
