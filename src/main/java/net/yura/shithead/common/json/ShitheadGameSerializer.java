package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.yura.shithead.common.ShitheadGame;

import java.io.IOException;

public class ShitheadGameSerializer extends JsonSerializer<ShitheadGame> {

    @Override
    public void serialize(ShitheadGame game, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String contextPlayerName = (String) serializers.getAttribute(PlayerSerializer.PLAYER_CONTEXT_KEY);

        gen.writeStartObject();
        gen.writeStringField("currentPlayerName", game.getCurrentPlayer().getName());

        if (contextPlayerName == null) {
            // Full serialization for persistence
            gen.writeObjectField("deck", game.getDeckCards());
        } else {
            // Player-specific view
            gen.writeNumberField("cardsInDeck", game.getDeckSize());
        }

        gen.writeObjectField("wastePile", game.getWastePile());
        gen.writeObjectField("players", game.getPlayers());
        gen.writeEndObject();
    }
}