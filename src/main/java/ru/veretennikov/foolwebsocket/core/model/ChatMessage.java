package ru.veretennikov.foolwebsocket.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.veretennikov.foolwebsocket.model.GameContent;

@Setter
@Getter
@NoArgsConstructor
public class ChatMessage {

    private MessageType type;
    private String sender;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

}
