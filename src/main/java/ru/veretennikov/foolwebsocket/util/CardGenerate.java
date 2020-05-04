package ru.veretennikov.foolwebsocket.util;

import ru.veretennikov.foolwebsocket.model.Card;

import java.util.Random;

public class CardGenerate {

    private static int[] suits = new int[]{0,1,2,3};
    private static String[] nominal = new String[]{"6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    public static Card getNewCard() {

        Random random = new Random();

        Card card = new Card();
        card.setNum(nominal[random.nextInt(nominal.length)]);
        card.setSuit(suits[random.nextInt(suits.length)]);
        card.setTrump(random.nextBoolean());

        return card;

    }

}
