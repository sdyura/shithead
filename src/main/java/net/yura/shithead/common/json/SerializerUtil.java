package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import net.yura.cardsengine.Card;
import net.yura.shithead.common.Player;
import net.yura.shithead.common.ShitheadGame;
import java.io.IOException;

public class SerializerUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ShitheadGame.class, new ShitheadGameSerializer());
        module.addSerializer(Player.class, new PlayerSerializer());
        // Use the built-in ToStringSerializer for Card, as its toString() method provides a good representation (e.g., "AS" for Ace of Spades).
        module.addSerializer(Card.class, new ToStringSerializer());
        module.addDeserializer(ShitheadGame.class, new ShitheadGameDeserializer());
        mapper.registerModule(module);
    }

    /**
     * Serializes the game state to JSON, optionally from the perspective of a specific player.
     *
     * @param game       The game object to serialize.
     * @param playerName The name of the player for whom the JSON is being generated.
     *                   If null, the full game state is serialized.
     * @return A JSON string representing the game state.
     * @throws JsonProcessingException If an error occurs during serialization.
     */
    public static String toJSON(ShitheadGame game, String playerName) throws JsonProcessingException {
        // Use a new ObjectMapper instance for each call to ensure thread safety with attributes
        ObjectMapper localMapper = mapper.copy();

        if (playerName != null) {
            localMapper.setConfig(localMapper.getSerializationConfig().withAttribute(PlayerSerializer.PLAYER_CONTEXT_KEY, playerName));
        }

        return localMapper.writerWithDefaultPrettyPrinter().writeValueAsString(game);
    }

    /**
     * Deserializes a ShitheadGame from a JSON string.
     *
     * @param json The JSON string representing the game state.
     * @return A new ShitheadGame object.
     * @throws IOException If an error occurs during deserialization.
     */
    public static ShitheadGame fromJSON(String json) throws IOException {
        return mapper.readValue(json, ShitheadGame.class);
    }
}