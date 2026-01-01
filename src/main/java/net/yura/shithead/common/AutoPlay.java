package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AutoPlay {

    /**
     * This method ONLY works for hand or UP cards
     */
    public static Card findBestVisibleCard(ShitheadGame game) {
        Player player = game.getCurrentPlayer();
        Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);
        List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

        Card bestNormalCard = null;
        Card bestPriorityCard = null;

        for (Card card : source) {
            if (game.isPlayable(card.getRank(), topCard)) {
                if (card.getRank() == Rank.TWO) {
                    if (bestPriorityCard == null || AcesHighCardComparator.getRankValue(card.getRank()) < AcesHighCardComparator.getRankValue(bestPriorityCard.getRank())) {
                        bestPriorityCard = card;
                    }
                } else {
                    if (bestNormalCard == null || AcesHighCardComparator.getRankValue(card.getRank()) < AcesHighCardComparator.getRankValue(bestNormalCard.getRank())) {
                        bestNormalCard = card;
                    }
                }
            }
        }

        return bestNormalCard != null ? bestNormalCard : bestPriorityCard;
    }

    public static List<Card> findBestVisibleCards(ShitheadGame game) {

        Card bestCard = findBestVisibleCard(game);

        if (bestCard == null) {
            return Collections.emptyList();
        }

        // two/ten/ace are the best cards that go on all cards, we wont want to give them away if we have 2
        if (bestCard.getRank() != Rank.TWO && bestCard.getRank() != Rank.TEN && bestCard.getRank() != Rank.ACE) {
            List<Card> cardsToPlay = new ArrayList<>();

            Player player = game.getCurrentPlayer();
            List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

            for (Card card : source) {
                if (card.getRank() == bestCard.getRank()) {
                    cardsToPlay.add(card);
                }
            }
            return cardsToPlay;
        }
        return Collections.singletonList(bestCard);
    }

    public static String getValidGameCommand(ShitheadGame game) {
        Player player = game.getCurrentPlayer();
        if (player.getHand().isEmpty() && player.getUpcards().isEmpty()) {
            return "play down 0";
        }

        List<Card> cards = AutoPlay.findBestVisibleCards(game);

        if (!cards.isEmpty()) {
            return "play " + (game.getCurrentPlayer().getHand().contains(cards.get(0)) ? "hand " : "up ") +
                    cards.stream().map(Object::toString).collect(Collectors.joining(" "));
        }

        if (!game.getDeck().getCards().isEmpty()) {
            return "play deck";
        }
        return "pickup";
    }
}
