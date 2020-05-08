package ru.veretennikov.foolwebsocket.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ServerChatMessage extends ChatMessage {
    private String content;
}
