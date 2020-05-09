package ru.veretennikov.foolwebsocket.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.veretennikov.foolwebsocket.model.GameContent;

@Setter
@Getter
@NoArgsConstructor
public abstract class ChatMessage {

    private MessageType type;
    private String sender;

    public enum MessageType {
        JOIN,
        MESSAGE,
        GAME_MESSAGE,
        LEAVE
    }

}
