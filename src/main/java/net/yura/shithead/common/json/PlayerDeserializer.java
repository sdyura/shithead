package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.yura.cardsengine.Card;
import net.yura.shithead.common.Player;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PlayerDeserializer extends StdDeserializer<Player> {

    public PlayerDeserializer() {
        this(null);
    }

    protected PlayerDeserializer(Class<?> vc) {
        super(vc);
    }

    private List<Card> readCards(JsonParser jp) throws IOException {
        return jp.readValueAs(new TypeReference<List<Card>>() {});
    }

    private List<Card> createNulls(int count) {
        return Collections.nCopies(count, null);
    }

    @Override
    public Player deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String playerName = null;
        List<Card> hand = null;
        List<Card> upcards = null;
        List<Card> downcards = null;

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            jp.nextToken(); // Move to the value

            if ("name".equals(fieldName)) {
                playerName = jp.getText();
            } else if ("hand".equals(fieldName) || "handCount".equals(fieldName)) {
                if (jp.currentToken() == JsonToken.START_ARRAY) {
                    hand = readCards(jp);
                } else {
                    hand = createNulls(jp.getIntValue());
                }
            } else if ("upcards".equals(fieldName) || "upcardsCount".equals(fieldName)) {
                if (jp.currentToken() == JsonToken.START_ARRAY) {
                    upcards = readCards(jp);
                } else {
                    upcards = createNulls(jp.getIntValue());
                }
            } else if ("downcards".equals(fieldName) || "downcardsCount".equals(fieldName)) {
                if (jp.currentToken() == JsonToken.START_ARRAY) {
                    downcards = readCards(jp);
                } else {
                    downcards = createNulls(jp.getIntValue());
                }
            } else {
                jp.skipChildren();
            }
        }

        Player player = new Player(playerName);
        if (hand != null) {
            player.getHand().addAll(hand);
        }
        if (upcards != null) {
            player.getUpcards().addAll(upcards);
        }
        if (downcards != null) {
            player.getDowncards().addAll(downcards);
        }

        return player;
    }
}