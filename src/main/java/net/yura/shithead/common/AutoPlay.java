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

        Card bestCard = null;
        Card aTwo = null; // Store a 2 in case it's the only option
        List<Card> source = !player.getHand().isEmpty() ? player.getHand() : player.getUpcards();

        for (Card card : source) {
            if (game.isPlayable(card.getRank(), topCard)) {
                // If the waste is empty, we want to save our 2s
                if (game.getWastePile().isEmpty() && card.getRank() == Rank.TWO) {
                    if (aTwo == null) {
                        aTwo = card;
                    }
                    // Continue to see if we have other, better options
                    continue;
                }

                if (bestCard == null || CardComparator.getRankValue(card.getRank()) < CardComparator.getRankValue(bestCard.getRank())) {
                    bestCard = card;
                }
            }
        }
        // If we found a non-2 card, play it.
        // Otherwise, play the 2 if it's our only option.
        return bestCard != null ? bestCard : aTwo;
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
