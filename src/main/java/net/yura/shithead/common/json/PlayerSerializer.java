package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.yura.shithead.common.Player;

import java.io.IOException;

public class PlayerSerializer extends JsonSerializer<Player> {

    public static final String PLAYER_CONTEXT_KEY = "player_context";

    @Override
    public void serialize(Player player, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String contextPlayerName = (String) serializers.getAttribute(PLAYER_CONTEXT_KEY);

        gen.writeStartObject();
        gen.writeStringField("name", player.getName());

        // Upcards are always visible
        gen.writeObjectField("upcards", player.getUpcards());

        boolean isContextPlayer = player.getName().equals(contextPlayerName);

        if (contextPlayerName == null || isContextPlayer) {
            // Full view: either no context or it's the context player
            gen.writeObjectField("hand", player.getHand());
            gen.writeObjectField("downcards", player.getDowncards());
        } else {
            // Restricted view for other players
            // Instead of serializing the cards, serialize their counts
            gen.writeNumberField("handCount", player.getHand().size());
            gen.writeNumberField("downcardsCount", player.getDowncards().size());
        }

        gen.writeEndObject();
    }
}