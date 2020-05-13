package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DurakPublicCurrentGameContent extends DurakPublicGameContent {
    private Card cardStep;
}
