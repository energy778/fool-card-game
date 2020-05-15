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
import ru.veretennikov.foolwebsocket.core.model.ServerChatMessage;
import ru.veretennikov.foolwebsocket.exception.GameException;
import ru.veretennikov.foolwebsocket.exception.GamePrivateException;
import ru.veretennikov.foolwebsocket.exception.PrivateException;
import ru.veretennikov.foolwebsocket.service.GameService;
import ru.veretennikov.foolwebsocket.service.GreetingService;
import ru.veretennikov.foolwebsocket.service.SendService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GreetingService greetingService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SendService sendService;

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

            gameService.checkCommand(incomeMessage.getContent(), sessionId);
            sendService.sendMessages(incomeMessage, ChatMessage.MessageType.GAME_MESSAGE, gameService.getContent(sessionId));

        } else if ("go".equalsIgnoreCase(incomeMessage.getContent())) {
//            попытка начать игру

            gameService.startGame(sessionId);
//            хода не было, можно сразу получать игровой контент и рассылать сообщения
            sendService.sendMessages(incomeMessage, ChatMessage.MessageType.START_GAME, gameService.getContent(sessionId));

        } else {
            sendService.sendMessages(incomeMessage, ChatMessage.MessageType.MESSAGE, null);
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

}
