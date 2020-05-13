package ru.veretennikov.foolwebsocket.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.veretennikov.foolwebsocket.model.GameContent;
import ru.veretennikov.foolwebsocket.model.DurakPublicGameContent;

@Setter
@Getter
@NoArgsConstructor
public class GameServerChatMessage extends ChatMessage {

    private GameContent gameContent;

    public GameServerChatMessage(DurakPublicGameContent gameContent) {
        super();
        this.gameContent = gameContent;
    }

}
