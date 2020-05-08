package ru.veretennikov.foolwebsocket.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientChatMessage extends ChatMessage{
    private String content;
}
