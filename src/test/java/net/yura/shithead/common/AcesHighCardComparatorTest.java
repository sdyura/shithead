package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcesHighCardComparatorTest {

    private final AcesHighCardComparator comparator = new AcesHighCardComparator();

    @Test
    void testCompare() {
        Card aceOfSpades = Card.getCardByRankSuit(Rank.ACE, Suit.SPADES);
        Card kingOfSpades = Card.getCardByRankSuit(Rank.KING, Suit.SPADES);
        Card kingOfHearts = Card.getCardByRankSuit(Rank.KING, Suit.HEARTS);
        Card twoOfClubs = Card.getCardByRankSuit(Rank.TWO, Suit.CLUBS);

        // Different ranks
        assertTrue(comparator.compare(twoOfClubs, kingOfSpades) < 0);
        assertTrue(comparator.compare(kingOfSpades, twoOfClubs) > 0);

        // Ace is high
        assertTrue(comparator.compare(kingOfSpades, aceOfSpades) < 0);
        assertTrue(comparator.compare(aceOfSpades, kingOfSpades) > 0);

        // Same rank, different suits
        assertTrue(comparator.compare(kingOfHearts, kingOfSpades) > 0);
        assertTrue(comparator.compare(kingOfSpades, kingOfHearts) < 0);

        // Identical cards
        assertEquals(0, comparator.compare(kingOfSpades, kingOfSpades));
    }

    @Test
    void testGetRankValue() {
        assertEquals(14, AcesHighCardComparator.getRankValue(Rank.ACE));
        assertEquals(2, AcesHighCardComparator.getRankValue(Rank.TWO));
        assertEquals(3, AcesHighCardComparator.getRankValue(Rank.THREE));
        assertEquals(4, AcesHighCardComparator.getRankValue(Rank.FOUR));
        assertEquals(5, AcesHighCardComparator.getRankValue(Rank.FIVE));
        assertEquals(6, AcesHighCardComparator.getRankValue(Rank.SIX));
        assertEquals(7, AcesHighCardComparator.getRankValue(Rank.SEVEN));
        assertEquals(8, AcesHighCardComparator.getRankValue(Rank.EIGHT));
        assertEquals(9, AcesHighCardComparator.getRankValue(Rank.NINE));
        assertEquals(10, AcesHighCardComparator.getRankValue(Rank.TEN));
        assertEquals(11, AcesHighCardComparator.getRankValue(Rank.JACK));
        assertEquals(12, AcesHighCardComparator.getRankValue(Rank.QUEEN));
        assertEquals(13, AcesHighCardComparator.getRankValue(Rank.KING));
    }
}
