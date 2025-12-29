package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardComparatorTest {

    private final CardComparator comparator = new CardComparator();

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
        assertEquals(14, CardComparator.getRankValue(Rank.ACE));
        assertEquals(2, CardComparator.getRankValue(Rank.TWO));
        assertEquals(3, CardComparator.getRankValue(Rank.THREE));
        assertEquals(4, CardComparator.getRankValue(Rank.FOUR));
        assertEquals(5, CardComparator.getRankValue(Rank.FIVE));
        assertEquals(6, CardComparator.getRankValue(Rank.SIX));
        assertEquals(7, CardComparator.getRankValue(Rank.SEVEN));
        assertEquals(8, CardComparator.getRankValue(Rank.EIGHT));
        assertEquals(9, CardComparator.getRankValue(Rank.NINE));
        assertEquals(10, CardComparator.getRankValue(Rank.TEN));
        assertEquals(11, CardComparator.getRankValue(Rank.JACK));
        assertEquals(12, CardComparator.getRankValue(Rank.QUEEN));
        assertEquals(13, CardComparator.getRankValue(Rank.KING));
    }
}
