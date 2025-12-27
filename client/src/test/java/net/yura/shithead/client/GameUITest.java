package net.yura.shithead.client;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import net.yura.shithead.common.Player;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameUITest {

    @Test
    public void testSortHand() {
        Player player = new Player("test");
        List<Card> hand = player.getHand();

        Card card1 = Card.getCardByRankSuit(Rank.ACE, Suit.SPADES);
        Card card2 = Card.getCardByRankSuit(Rank.KING, Suit.HEARTS);
        Card card3 = Card.getCardByRankSuit(Rank.QUEEN, Suit.DIAMONDS);

        hand.add(card2);
        hand.add(card1);
        hand.add(card3);

        GameUI.sortHand(player);

        List<Card> expected = Arrays.asList(card3, card2, card1);
        assertEquals(expected, hand);

        // test reverse
        GameUI.sortHand(player);

        Collections.reverse(expected);
        assertEquals(expected, hand);
    }
}
