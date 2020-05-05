package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CardDeck {

    private LinkedList<Card> cards;
    private int trumpSuit;

    public CardDeck() {
        this.cards = new LinkedList<>();
        this.trumpSuit = 0;
    }

    public boolean isEmpty() {
        if (cards == null)
            return true;
        return cards.isEmpty();
    }

    public Card pop() {
        if (cards == null)
            return null;
        return cards.pop();
    }

    public int size() {
        if (cards == null)
            return 0;
        return cards.size();
    }


    public List<Card> getSomeCards(int size) {

        if (size == 0)
            size = 4;

        Random random = new Random();

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < random.nextInt(size)+1; i++) {
            if (this.isEmpty())
                break;
            Card curCard = this.pop();
            curCard.setTrump(curCard.getSuit() == trumpSuit);
            cards.add(curCard);
        }

        cards.sort(Comparator.comparing(Card::getRank));

        return cards;

    }

}
