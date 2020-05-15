package ru.veretennikov.foolwebsocket.service;

import ru.veretennikov.foolwebsocket.core.model.ChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ClientChatMessage;
import ru.veretennikov.foolwebsocket.model.GameContent;

import java.util.List;

public interface SendService {
    void sendMessages(ClientChatMessage incomeMessage, ChatMessage.MessageType messageType, List<GameContent> gameContents);
}
