package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import java.util.Comparator;

public class CardComparator implements Comparator<Card> {

    @Override
    public int compare(Card c1, Card c2) {
        int rankComparison = Integer.compare(c1.getRank().toInt(), c2.getRank().toInt());
        if (rankComparison != 0) {
            return rankComparison;
        } else {
            return Integer.compare(c1.getSuit().toInt(), c2.getSuit().toInt());
        }
    }
}
