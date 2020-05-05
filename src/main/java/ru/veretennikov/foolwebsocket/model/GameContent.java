package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GameContent {

    private List<Card> cards;
    private int cardDeckSize;
    private int trumpSuit;
    private Card trump;

    public GameContent() {
        cards = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "GameContent{" +
                "cards=" + cards +
                ", cardDeckSize=" + cardDeckSize +
                ", trump=" + trump +
                ", trumpSuit=" + trumpSuit +
                '}';
    }
}
