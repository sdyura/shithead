package net.yura.shithead.uicomponents;

import net.yura.cardsengine.Card;

import java.util.List;

/**
 * converts action into string command
 */
public interface GameViewListener {

    void swapCards(Card card1, Card card2);

    void playVisibleCard(boolean hand, List<Card> card);

    void pickUpWaste();

    void playDeck();

    void playDowncard();

    void updateButton();
}
