package ru.veretennikov.foolwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import ru.veretennikov.foolwebsocket.core.model.ChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ClientChatMessage;
import ru.veretennikov.foolwebsocket.core.model.GameServerChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ServerChatMessage;
import ru.veretennikov.foolwebsocket.exception.GameException;
import ru.veretennikov.foolwebsocket.exception.GamePrivateException;
import ru.veretennikov.foolwebsocket.exception.PrivateException;
import ru.veretennikov.foolwebsocket.model.GameContent;
import ru.veretennikov.foolwebsocket.model.PrivateGameContent;
import ru.veretennikov.foolwebsocket.model.PublicGameContent;
import ru.veretennikov.foolwebsocket.service.GameService;
import ru.veretennikov.foolwebsocket.service.GreetingService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GreetingService greetingService;
    private final SimpMessageSendingOperations messagingTemplate;

//    все сообщения от клиентов, направленные по адресу, начинающемуся с /app, будут перенаправлены в соответствующие методы
//    Например, сообщение, направленное по адресу /app/chat.addUser будет перенаправлено в метод addUser()
//              сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage()

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/events")
    public ServerChatMessage addUser(@Payload ClientChatMessage incomeMessage, SimpMessageHeaderAccessor headerAccessor) {

        String sender = incomeMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", sender);
        gameService.addUser(sender, headerAccessor.getSessionId());

        ServerChatMessage message = new ServerChatMessage();
        message.setType(ChatMessage.MessageType.JOIN);
        message.setSender(sender);
        message.setContent(greetingService.getGreeting(sender));

        return message;

    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessageHandler(@Payload ClientChatMessage incomeMessage, SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        if (gameService.isGameStarted()) {
//            делают ход (нападают, подкидывают, отбиваются, говорят "бито", говорят "пас")
//            пытаются написать во время игры (не будучи игроком, не в свою очередь, не той картой)

            gameService.processingCommand(incomeMessage.getContent(), sessionId);
            sendMessages(incomeMessage, ChatMessage.MessageType.GAME_MESSAGE, gameService.getContent(sessionId));

        } else if ("go".equalsIgnoreCase(incomeMessage.getContent())) {
//            попытка начать игру

            gameService.startGame(sessionId);
//            хода не было, можно сразу получать игровой контент и рассылать сообщения
            sendMessages(incomeMessage, ChatMessage.MessageType.START_GAME, gameService.getContent(sessionId));

        } else {
            sendMessages(incomeMessage, ChatMessage.MessageType.MESSAGE, null);
        }

    }

    @MessageExceptionHandler
    @SendTo(value = "/topic/errors")
    public String exceptionHandler(RuntimeException e) {
        return e.getMessage();
    }

    @MessageExceptionHandler
    public void privateExceptionHandler(PrivateException privateException) {
        messagingTemplate.convertAndSend("/topic/game/errors/" + privateException.getSessionId(), privateException.getMessage());
    }

    @MessageExceptionHandler
    @SendTo(value = "/topic/game/errors")
    public String gameExceptionHandler(GameException e) {
        return e.getMessage();
    }

    @MessageExceptionHandler
    public void gamePrivateExceptionHandler(GamePrivateException privateException) {
        messagingTemplate.convertAndSend("/topic/game/errors/" + privateException.getSessionId(), privateException.getMessage());
    }


    private void sendMessages(@Payload ClientChatMessage incomeMessage, ChatMessage.MessageType messageType, List<GameContent> gameContents) {

        if (gameContents == null){

//            просто болтают
            ServerChatMessage chatMessage = new ServerChatMessage(incomeMessage.getContent());
            chatMessage.setType(messageType);
            chatMessage.setSender(incomeMessage.getSender());
            sendPublicMessage(chatMessage);

        } else {

            for (GameContent gameContent : gameContents) {
                // TODO: 011 11.05.20 разделить игровой контент на стартовый и нестартовый
//                PrivateGameContent и PublicGameContent разделить на стартовы и нет
                if (gameContent instanceof PrivateGameContent){
                    buildSendPrivateMessage(gameContent, incomeMessage, messageType);
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

    private void buildSendPrivateMessage(GameContent gameContent, ClientChatMessage incomeMessage, ChatMessage.MessageType messageType) {
        GameServerChatMessage chatMessage = new GameServerChatMessage();
        chatMessage.setSender(incomeMessage.getSender());         // TODO: 009 09.05.20 не всегда нужно знать, кто был инициатором. но пока оставим это на усмотрение фронта
        chatMessage.setType(messageType);
        chatMessage.setGameContent(gameContent);
//        messagingTemplate.convertAndSendToUser(((PrivateGameContent) gameContent).getUserId(), "/topic/private/", chatMessage);     // не взлетает в разных вариациях, надоело бороться
        sendMessage("/topic/private/" + ((PrivateGameContent) gameContent).getUserId(), chatMessage);
    }

    private void sendPublicMessage(ChatMessage chatMessage) {
        sendMessage("/topic/public", chatMessage);
    }

    private void sendMessage(String sendTo, ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(sendTo, chatMessage);
    }

}
