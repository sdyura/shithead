package net.yura.shithead.common;

import net.yura.cardsengine.Card;

import java.util.List;

public class AutoPlay {

    /**
     * This method ONLY works for hand or UP cards
     */
    public static Card findBestVisibleCard(ShitheadGame game) {

        Player player = game.getCurrentPlayer();

        Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);

        Card bestCard = null;
        List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

        for (Card card : source) {
            if (game.isPlayable(card.getRank(), topCard)) {
                if (bestCard == null || CardComparator.getRankValue(card.getRank()) < CardComparator.getRankValue(bestCard.getRank())) {
                    bestCard = card;
                }
            }
        }
        return bestCard;
    }
}
