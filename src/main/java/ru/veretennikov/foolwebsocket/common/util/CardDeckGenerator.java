package ru.veretennikov.foolwebsocket.common.util;

import ru.veretennikov.foolwebsocket.model.Card;
import ru.veretennikov.foolwebsocket.model.CardDeck;
import ru.veretennikov.foolwebsocket.model.Rank;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.shuffle;

public class CardDeckGenerator {

    private static final int[] suits = new int[]{0,1,2,3};
    private static final Rank[] ranks = Rank.values();

    public static CardDeck newCardDeck(List<Rank> ranks) {

        if (ranks == null)
            ranks = Arrays.asList(CardDeckGenerator.ranks);

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

        shuffle(cardDeck.getCards());

//        тут небольшая особенность. козырь определяется сразу, т.е. ДО раздачи карт игрокам
        cardDeck.pickTrump();

        return cardDeck;

    }

}
