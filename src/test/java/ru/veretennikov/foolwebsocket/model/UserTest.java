package ru.veretennikov.foolwebsocket.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.veretennikov.foolwebsocket.common.util.CardDeckGenerator;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.veretennikov.foolwebsocket.model.Rank.*;

class UserTest {

    private CardDeck cardDeck;
    private User user;

    @BeforeEach
    public void init(){
        cardDeck = CardDeckGenerator.newCardDeck(Arrays.asList(SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE));
        user = new User("1", "Братишка");
    }

    @Test
    void pickCards() {

        assertNotNull(user.getHand());

        int before = user.getHand().getCards().size();

        user.pickCards(cardDeck, 6);
        int after1 = user.getHand().getCards().size();
        assertEquals(6, after1 - before);

        user.pickCards(cardDeck, 0);
        int after2 = user.getHand().getCards().size();
        assertEquals(after1, after2);

        user.pickCards(cardDeck, 100);
        int after3 = user.getHand().getCards().size();
        assertEquals(36, after3);

    }

    @Test
    void pickCardsAll() {

        assertNotNull(user.getHand());

        int before = user.getHand().getCards().size();
        user.pickCards(Collections.singletonList(new Card()));
        assertEquals(user.getHand().getCards().size(), before +1);

    }

}