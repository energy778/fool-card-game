package ru.veretennikov.foolwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import ru.veretennikov.foolwebsocket.core.model.ChatMessage;
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
    public ChatMessage sendMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        GameContent gameContent = gameService.processingCommand(message.getContent(), sessionId);
        message.setGameContent(gameContent);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("confidential");
        messagingTemplate.convertAndSend("/topic/private/" + sessionId, chatMessage);
//        messagingTemplate.convertAndSendToUser(sessionId, "/topic/public", chatMessage);

        return message;

    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        // Add username in web socket session
        String sender = chatMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", sender);
//        chatMessage.setType(ChatMessage.MessageType.JOIN);  определили на клиенте
        gameService.addUser(sender, headerAccessor.getSessionId());
        chatMessage.setContent(greetingService.getGreeting(sender));

        return chatMessage;

    }

//    все сообщения от клиентов, направленные по адресу, начинающемуся с /app, будут перенаправлены в соответствующие методы
//    Имелись в виду как раз методы, аннотированные @MessageMapping
//    Например, сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage()
//    А например, сообщение, направленное по адресу/app/chat.addUser будет перенаправлено в метод addUser()

}
