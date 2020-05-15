package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class CardDeck {

    private LinkedList<Card> cards;
    private Card trump;
    private int trumpSuit;

    public CardDeck() {
        this.cards = new LinkedList<>();
        this.trumpSuit = 0;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Card pop() {
        return cards.pop();
    }

    public int size() {
        return cards.size();
    }


    public List<Card> getSomeCards(int size) {

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (this.isEmpty())
                break;
            Card curCard = this.pop();
            curCard.setTrump(curCard.getSuit() == trumpSuit);
            cards.add(curCard);
        }

        return cards;

    }

//    козырь. карта берется сверху и после демонстрации кладется под колоду
//    для разных игр может использоваться по-разному. в некоторых играх и вовсе отсутствовать. но пока пусть так
    public void pickTrump() {
        Card trump = cards.pop();
        cards.addLast(trump);
        this.trump = trump;
        this.trumpSuit = trump.getSuit();
        cards.forEach(card -> card.setTrump(card.getSuit() == this.trumpSuit));
    }

}
