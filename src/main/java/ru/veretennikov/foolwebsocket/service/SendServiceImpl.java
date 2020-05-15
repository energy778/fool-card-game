package ru.veretennikov.foolwebsocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import ru.veretennikov.foolwebsocket.core.model.ChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ClientChatMessage;
import ru.veretennikov.foolwebsocket.core.model.GameServerChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ServerChatMessage;
import ru.veretennikov.foolwebsocket.model.GameContent;
import ru.veretennikov.foolwebsocket.model.PrivateGameContent;
import ru.veretennikov.foolwebsocket.model.PublicGameContent;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SendServiceImpl implements SendService {

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void sendMessages(ClientChatMessage incomeMessage, ChatMessage.MessageType messageType, List<GameContent> gameContents) {

        if (gameContents == null){
//            просто болтают

            ServerChatMessage chatMessage = new ServerChatMessage(incomeMessage.getContent());
            chatMessage.setType(messageType);
            chatMessage.setSender(incomeMessage.getSender());
            sendPublicMessage(chatMessage);

        } else {
//            игра

            for (GameContent gameContent : gameContents) {
                if (gameContent instanceof PrivateGameContent){
                    buildSendPrivateMessage((PrivateGameContent) gameContent, incomeMessage, messageType);
                } else if (gameContent instanceof PublicGameContent) {
                    buildSendPublicMessage(gameContent, incomeMessage, messageType);
                } else {
//                nothing yet
                }
            }

        }

    }

    private void buildSendPublicMessage(GameContent gameContent, ClientChatMessage incomeMessage, ChatMessage.MessageType messageType) {
        GameServerChatMessage chatMessage = new GameServerChatMessage();
        chatMessage.setType(messageType);
        chatMessage.setSender(incomeMessage.getSender());
        chatMessage.setGameContent(gameContent);
        sendPublicMessage(chatMessage);
    }

    private void buildSendPrivateMessage(PrivateGameContent gameContent, ClientChatMessage incomeMessage, ChatMessage.MessageType messageType) {
        GameServerChatMessage chatMessage = new GameServerChatMessage();
        chatMessage.setSender(incomeMessage.getSender());         // TODO: 009 09.05.20 не всегда нужно знать, кто был инициатором. но пока оставим это на усмотрение фронта
        chatMessage.setType(messageType);
        chatMessage.setGameContent(gameContent);
//        messagingTemplate.convertAndSendToUser(((PrivateGameContent) gameContent).getUserId(), "/topic/private/", chatMessage);     // не взлетает в разных вариациях, надоело бороться
        sendMessage("/topic/private/" + gameContent.getUserId(), chatMessage);
    }

    private void sendPublicMessage(ChatMessage chatMessage) {
        sendMessage("/topic/public", chatMessage);
    }

    private void sendMessage(String sendTo, ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(sendTo, chatMessage);
    }

}
