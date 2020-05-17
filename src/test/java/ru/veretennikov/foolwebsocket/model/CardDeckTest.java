package ru.veretennikov.foolwebsocket.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.veretennikov.foolwebsocket.common.util.CardDeckGenerator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CardDeckTest {

    private CardDeck cardDeck;

    @BeforeEach
    public void init(){
        cardDeck = CardDeckGenerator.newCardDeck(Arrays.asList(Rank.values()));
    }

    @Test
    void pop() {
        int oldSize = cardDeck.size();
        Card pop = cardDeck.pop();
        assertNotNull(pop);
        assertTrue(cardDeck.size() < oldSize);
    }

    @Test
    void getSomeCards() {
        List<Card> someCards = cardDeck.getSomeCards(3);
        assertNotNull(someCards);
        assertEquals(3, someCards.size());
    }

    @Test
    void pickTrump() {
        int allCards = cardDeck.getCards().size();
        cardDeck.pickTrump();
        long onlyTrump = cardDeck.getCards().stream().filter(Card::isTrump).count();
        assertEquals(allCards, (onlyTrump * 4));
    }

}