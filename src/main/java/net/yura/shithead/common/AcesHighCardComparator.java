package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import java.util.Comparator;

public class AcesHighCardComparator implements Comparator<Card> {

    public static int getRankValue(Rank rank) {
        int rankValue = rank.toInt();
        if (rankValue == 1) { // Ace
            return 14;
        }
        return rankValue;
    }

    @Override
    public int compare(Card c1, Card c2) {
        int rank1 = getRankValue(c1.getRank());
        int rank2 = getRankValue(c2.getRank());

        int rankComparison = Integer.compare(rank1, rank2);
        if (rankComparison != 0) {
            return rankComparison;
        } else {
            return Integer.compare(c1.getSuit().toInt(), c2.getSuit().toInt());
        }
    }
}
