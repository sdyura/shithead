package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import java.util.Comparator;

public class CardComparator implements Comparator<Card> {

    @Override
    public int compare(Card c1, Card c2) {
        int rank1 = c1.getRank().toInt();
        int rank2 = c2.getRank().toInt();
        if (rank1 == 1) rank1 = 14;
        if (rank2 == 1) rank2 = 14;

        int rankComparison = Integer.compare(rank1, rank2);
        if (rankComparison != 0) {
            return rankComparison;
        } else {
            return Integer.compare(c1.getSuit().toInt(), c2.getSuit().toInt());
        }
    }
}
