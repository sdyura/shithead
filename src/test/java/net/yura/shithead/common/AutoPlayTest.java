package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoPlayTest {

    @Test
    public void testFindBestVisibleCards() {
        // Given
        ShitheadGame game = mock(ShitheadGame.class);
        Player player = mock(Player.class);
        when(game.getCurrentPlayer()).thenReturn(player);

        List<Card> hand = Arrays.asList(
                Card.getCardByRankSuit(Rank.ACE, Suit.SPADES),
                Card.getCardByRankSuit(Rank.EIGHT, Suit.CLUBS),
                Card.getCardByRankSuit(Rank.EIGHT, Suit.DIAMONDS)
        );
        when(player.getHand()).thenReturn(hand);

        when(game.isPlayable(Rank.ACE, null)).thenReturn(true);
        when(game.isPlayable(Rank.EIGHT, null)).thenReturn(true);

        // When
        List<Card> bestCards = AutoPlay.findBestVisibleCards(game);

        // Then
        assertEquals(2, bestCards.size());
        assertTrue(bestCards.contains(Card.getCardByRankSuit(Rank.EIGHT, Suit.CLUBS)));
        assertTrue(bestCards.contains(Card.getCardByRankSuit(Rank.EIGHT, Suit.DIAMONDS)));
    }
}
