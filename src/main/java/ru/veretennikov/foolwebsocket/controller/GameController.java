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
import ru.veretennikov.foolwebsocket.model.PrivateGameContent;
import ru.veretennikov.foolwebsocket.service.GameService;
import ru.veretennikov.foolwebsocket.service.GreetingService;

import java.util.Map;

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
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ClientChatMessage incomeMessage, SimpMessageHeaderAccessor headerAccessor) {

        // TODO: 009 09.05.20 временное решение, исправить
        ChatMessage message = new ServerChatMessage("");

        String sessionId = headerAccessor.getSessionId();

        if (gameService.isGameStarted()) {
//            делают ход (нападают, подкидывают, отбиваются, говорят "бито", говорят "пас")
//            пытаются написать во время игры (не будучи игроком, не в свою очередь, не той картой)

            gameService.processingCommand(incomeMessage.getContent(), sessionId);

            sendPrivateGameContent(incomeMessage);

            message = new GameServerChatMessage(gameService.getPublicContent(sessionId));
            message.setType(ChatMessage.MessageType.GAME_MESSAGE);

        } else if ("go".equalsIgnoreCase(incomeMessage.getContent())) {
//            попытка начать игру

            gameService.startGame(sessionId);
//            хода не было, можно сразу получать игровой контент

            sendPrivateGameContent(incomeMessage);

            message = new GameServerChatMessage(gameService.getPublicContent(sessionId));
            message.setType(ChatMessage.MessageType.GAME_MESSAGE);

        } else {
//            просто болтают

            message = new ServerChatMessage(incomeMessage.getContent());
            message.setType(ChatMessage.MessageType.MESSAGE);

        }

        message.setSender(incomeMessage.getSender());

        return message;

    }

    private void sendPrivateGameContent(ClientChatMessage incomeMessage) {
        Map<String, PrivateGameContent> privateContents = gameService.getPrivateContent();
        for (Map.Entry<String, PrivateGameContent> privateGameContentEntry : privateContents.entrySet()) {
            GameServerChatMessage chatMessage = new GameServerChatMessage();
            chatMessage.setSender(incomeMessage.getSender());                       // TODO: 009 09.05.20 не всегда нужно знать, кто был инициатором
            chatMessage.setType(ChatMessage.MessageType.GAME_MESSAGE);
            chatMessage.setGameContent(privateGameContentEntry.getValue());
//            messagingTemplate.convertAndSendToUser(privateGameContentEntry.getKey(), "/topic/private/", chatMessage);     // не взлетает в разных вариациях, надоело бороться
            messagingTemplate.convertAndSend("/topic/private/" + privateGameContentEntry.getKey(), chatMessage);
        }
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

    @MessageExceptionHandler
    @SendTo(value = "/topic/errors")
    public String exceptionHandler(RuntimeException e) {
        return e.getMessage();
    }

    @MessageExceptionHandler
    public void privateExceptionHandler(PrivateException privateException) {
        messagingTemplate.convertAndSend("/topic/game/errors/" + privateException.getSessionId(), privateException.getMessage());
    }

}
