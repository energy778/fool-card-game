package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DurakPublicCurrentGameContent extends DurakPublicGameContent {

    private int trumpSuit;
    private int cardDeckSize;

    private int turn;                       // ?
    private Card cardStep;                  // ?

    @Override
    public String toString() {
        return "DurakPublicCurrentGameContent{" +
                "trumpSuit=" + trumpSuit +
                ", cardDeckSize=" + cardDeckSize +
                ", turn=" + turn +
                '}';
    }

}
