package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShitheadGameMiscTest {

    @Test
    public void testSortHand() {
        ShitheadGame game = new ShitheadGame(1);
        Player player = game.getPlayers().get(0);
        List<Card> hand = player.getHand();

        Card card1 = Card.getCardByRankSuit(Rank.ACE, Suit.SPADES);
        Card card2 = Card.getCardByRankSuit(Rank.KING, Suit.HEARTS);
        Card card3 = Card.getCardByRankSuit(Rank.QUEEN, Suit.DIAMONDS);

        hand.add(card2);
        hand.add(card1);
        hand.add(card3);

        game.sortHand(player.getName());

        List<Card> expected = Arrays.asList(card1, card3, card2);
        assertEquals(expected, hand);

        // test reverse
        game.sortHand(player.getName());

        Collections.reverse(expected);
        assertEquals(expected, hand);
    }
}
