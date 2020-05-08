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
import ru.veretennikov.foolwebsocket.model.GameContent;
import ru.veretennikov.foolwebsocket.service.GameService;
import ru.veretennikov.foolwebsocket.service.GreetingService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GreetingService greetingService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public GameServerChatMessage sendMessage(@Payload ClientChatMessage incomeMessage, SimpMessageHeaderAccessor headerAccessor) {

        GameServerChatMessage message = new GameServerChatMessage();
        message.setSender(incomeMessage.getSender());
        message.setType(incomeMessage.getType());

        String sessionId = headerAccessor.getSessionId();
        GameContent gameContent = gameService.processingCommand(incomeMessage.getContent(), sessionId);
        message.setGameContent(gameContent);

//        // TODO: 008 08.05.20 отделять личные сообщения от публичных
//        ChatMessage chatMessage = new ChatMessage();
//        chatMessage.setContent("confidential");
//        messagingTemplate.convertAndSend("/topic/private/" + sessionId, chatMessage);
////        messagingTemplate.convertAndSendToUser(sessionId, "/topic/public", chatMessage);

        return message;

    }

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
//    все сообщения от клиентов, направленные по адресу, начинающемуся с /app, будут перенаправлены в соответствующие методы
//    Например, сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage()
//    А например, сообщение, направленное по адресу/app/chat.addUser будет перенаправлено в метод addUser()

    @MessageExceptionHandler
    @SendTo(value = "/topic/game/errors")
    public String gameExceptionHandler(GameException e) {
        return e.getMessage();
    }

    @MessageExceptionHandler
    @SendTo(value = "/topic/errors")
    public String exceptionHandler(RuntimeException e) {
        return e.getMessage();
    }

}
