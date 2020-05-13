package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair {

    private Card attacker;
    private Card defender;

    public Pair(Card attacker) {
        this.attacker = attacker;
    }

}
