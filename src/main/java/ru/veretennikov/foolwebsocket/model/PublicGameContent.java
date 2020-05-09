package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PublicGameContent extends GameContent {

    private int turn;
    private int cardDeckSize;
    private int trumpSuit;
    private Card trump;

    @Override
    public String toString() {
        return "GameContent{" +
                "cards=" + super.getCards() +
                ", cardDeckSize=" + cardDeckSize +
                ", trumpSuit=" + trumpSuit +
                ", trump=" + trump +
                '}';
    }

}
