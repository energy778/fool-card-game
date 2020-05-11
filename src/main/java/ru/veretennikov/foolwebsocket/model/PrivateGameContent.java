package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PrivateGameContent extends GameContent{
    private String userId;
}
