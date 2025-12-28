package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoPlay {

    /**
     * This method ONLY works for hand or UP cards
     */
    public static List<Card> findBestVisibleCards(ShitheadGame game) {

        Player player = game.getCurrentPlayer();

        Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);

        Card bestCard = null;
        List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

        for (Card card : source) {
            if (game.isPlayable(card.getRank(), topCard)) {
                if (bestCard == null || CardComparator.getRankValue(card.getRank()) < CardComparator.getRankValue(bestCard.getRank()) || bestCard.getRank() == Rank.TWO) {
                    bestCard = card;
                }
            }
        }

        if (bestCard == null) {
            return Collections.emptyList();
        }

        List<Card> cardsToPlay = new ArrayList<>();
        for (Card card : source) {
            if (card.getRank() == bestCard.getRank()) {
                cardsToPlay.add(card);
            }
        }
        return cardsToPlay;
    }
}
