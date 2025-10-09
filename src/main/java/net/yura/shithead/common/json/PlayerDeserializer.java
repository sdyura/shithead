package net.yura.shithead.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
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
        String playerName = null;
        // Initialize to empty lists to handle spectator view where these fields might be missing
        List<Card> hand = new java.util.ArrayList<>();
        List<Card> upcards = new java.util.ArrayList<>();
        List<Card> downcards = new java.util.ArrayList<>();

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            jp.nextToken(); // Move to the value

            if ("name".equals(fieldName)) {
                playerName = jp.getText();
            } else if ("hand".equals(fieldName) || "handCount".equals(fieldName)) {
                List<Card> cards = GameDeserializer.readCardsOrCount(jp);
                if (cards != null) hand = cards;
            } else if ("upcards".equals(fieldName) || "upcardsCount".equals(fieldName)) {
                List<Card> cards = GameDeserializer.readCardsOrCount(jp);
                if (cards != null) upcards = cards;
            } else if ("downcards".equals(fieldName) || "downcardsCount".equals(fieldName)) {
                List<Card> cards = GameDeserializer.readCardsOrCount(jp);
                if (cards != null) downcards = cards;
            } else {
                jp.skipChildren();
            }
        }

        Player player = new Player(playerName);
        player.getHand().addAll(hand);
        player.getUpcards().addAll(upcards);
        player.getDowncards().addAll(downcards);

        return player;
    }
}