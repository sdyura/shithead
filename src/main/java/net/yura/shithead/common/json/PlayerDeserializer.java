package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.yura.cardsengine.Card;
import net.yura.shithead.common.Player;
import java.io.IOException;
import java.util.List;

public class PlayerDeserializer extends StdDeserializer<Player> {

    public PlayerDeserializer() {
        this(null);
    }

    protected PlayerDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Player deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);

        String name = node.get("name").asText();
        Player player = new Player(name);

        if (node.has("hand")) {
            List<Card> hand = mapper.convertValue(node.get("hand"), new TypeReference<List<Card>>() {});
            player.getHand().addAll(hand);
        }

        if (node.has("upcards")) {
            List<Card> upcards = mapper.convertValue(node.get("upcards"), new TypeReference<List<Card>>() {});
            player.getUpcards().addAll(upcards);
        }

        if (node.has("downcards")) {
            List<Card> downcards = mapper.convertValue(node.get("downcards"), new TypeReference<List<Card>>() {});
            player.getDowncards().addAll(downcards);
        }

        return player;
    }
}