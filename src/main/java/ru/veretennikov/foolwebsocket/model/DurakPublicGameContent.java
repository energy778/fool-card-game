package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DurakPublicGameContent extends PublicGameContent {
    private DurakGameEvent gameEvent;
    private Card trump;
}
