package net.yura.shithead.uicomponents;

import net.yura.cardsengine.Card;

/**
 * converts action into string command
 */
public interface GameViewListener {

    void swapCards(Card card1, Card card2);

    void playVisibleCard(boolean hand, Card card);

    void pickUpWaste();
}
