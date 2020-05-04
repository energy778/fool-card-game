package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;

@Getter
@Setter
@NoArgsConstructor
public class CardDeck {
    private LinkedList<Card> cards;
    private int trumpSuit;
}
