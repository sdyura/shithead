package net.yura.shithead.uicomponents;

import java.util.List;
import net.yura.cardsengine.Card;

/**
 * converts action into string command
 */
public interface GameViewListener {

    void swapCards(Card card1, Card card2);

    void playVisibleCards(List<Card> cards, CardLocation location);

    void pickUpWaste();

    void playDeck();

    void playDowncard();

    void selectionChanged(List<Card> list, CardLocation location);
}
