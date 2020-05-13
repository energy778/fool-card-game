package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DurakPublicGameContent extends GameContent {

    private int turn;
    private int cardDeckSize;
    private int trumpSuit;
    private Card trump;
    private DurakGameEvent gameEvent;

    @Override
    public String toString() {
        return "GameContent{" +
                "cards=" + super.getCards() +
                ", cardDeckSize=" + cardDeckSize +
                ", trumpSuit=" + trumpSuit +
                '}';
    }

}
