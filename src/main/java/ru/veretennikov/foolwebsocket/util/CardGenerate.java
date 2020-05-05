package ru.veretennikov.foolwebsocket.util;

import ru.veretennikov.foolwebsocket.model.Card;
import ru.veretennikov.foolwebsocket.model.CardDeck;
import ru.veretennikov.foolwebsocket.model.Rank;

import java.util.Random;

import static java.util.Collections.shuffle;

// TODO: 004 04.05.20 можно переписать на спринг бин (сервис)
public class CardGenerate {

    private static int[] suits = new int[]{0,1,2,3};
    private static Rank[] ranks = Rank.values();

    public static CardDeck newCardDeck() {

        CardDeck cardDeck = new CardDeck();

        for (Rank rank : ranks) {
            for (int suit : suits) {
                Card card = new Card();
                card.setRank(rank);
                card.setSuit(suit);
                card.setTrump(false);
                cardDeck.getCards().add(card);
            }
        }

        Random random = new Random();
        cardDeck.setTrumpSuit(suits[random.nextInt(suits.length)]);

        shuffle(cardDeck.getCards());

        return cardDeck;

    }

}
