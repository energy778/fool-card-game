package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DurakPublicStartGameContent extends DurakPublicGameContent {
    private Card reasonCard;
}
