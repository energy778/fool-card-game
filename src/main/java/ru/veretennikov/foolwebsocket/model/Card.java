package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Card {
    private Rank rank;
    private int suit;
    private boolean trump;
}
