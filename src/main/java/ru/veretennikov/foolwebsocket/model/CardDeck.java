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
//        if (cards == null)
//            return true;
        return cards.isEmpty();
    }

    public Card pop() {
//        if (cards == null)
//            return null;
        return cards.pop();
    }

    public int size() {
//        if (cards == null)
//            return 0;
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

        // TODO: 005 05.05.20 добавить опцию сортировки по разным критериям
        cards.sort(Comparator.comparing(Card::getRank));

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
