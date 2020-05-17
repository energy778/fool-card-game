package ru.veretennikov.foolwebsocket.common.util;

import org.junit.jupiter.api.Test;
import ru.veretennikov.foolwebsocket.model.CardDeck;
import ru.veretennikov.foolwebsocket.model.Rank;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.veretennikov.foolwebsocket.model.Rank.*;

class CardDeckGeneratorTest {

    @Test
    void newCardDeck() {

        List<Rank> ranks = Arrays.asList(SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE);
        CardDeck cardDeck = CardDeckGenerator.newCardDeck(ranks);

        assertNotNull(cardDeck);

        assertFalse(cardDeck.isEmpty());
        assertEquals(cardDeck.size(), (4 * ranks.size()));

        assertNotNull(cardDeck.getTrump());
        assertTrue(cardDeck.getTrumpSuit() >=0 && cardDeck.getTrumpSuit() < 4);

    }

}