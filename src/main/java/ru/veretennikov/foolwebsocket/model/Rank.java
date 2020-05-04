package ru.veretennikov.foolwebsocket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Rank {

    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A")
    ;

    private String name;

    Rank(String name) {
        this.name = name;
    }

    @JsonCreator
    public static Rank fromString(String name) {
        return name == null
                ? null
                : Rank.valueOf(name.toUpperCase());
    }

    @JsonValue
    public String getName() {
        return name;
    }

}
