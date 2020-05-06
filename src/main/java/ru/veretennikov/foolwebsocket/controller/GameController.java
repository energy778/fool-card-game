package ru.veretennikov.foolwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import ru.veretennikov.foolwebsocket.core.model.ChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ChatMessageSimple;
import ru.veretennikov.foolwebsocket.service.GameService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessageSimple message) {

        // TODO: 006 06.05.20 фильтровать по типу сообщению. например для LEAVE - просто возврат сообщения на клиент

//        генерация новой колоды. пока что через "особое" сообщение по текущему же адресу (только при условии, что в колоде уже не осталось карт)
//        но потом сделать по кнопке и отдельным методом контроллера
        gameService.checkNewGame(message.getContent());

        ChatMessage newMessage = new ChatMessage();
        newMessage.setSender(message.getSender());
        newMessage.setType(ChatMessage.MessageType.CHAT);
//        String enteredCard = message.getContent();        надо генерировать карту на основе присланной информации
        newMessage.setContent(gameService.getContent());

        return newMessage;

    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        // Add username in web socket session
        String sender = chatMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", sender);
        gameService.addUser(sender, headerAccessor.getSessionId());

        return chatMessage;

    }

//    все сообщения от клиентов, направленные по адресу, начинающемуся с /app, будут перенаправлены в соответствующие методы
//    Имелись в виду как раз методы, аннотированные @MessageMapping
//    Например, сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage()
//    А например, сообщение, направленное по адресу/app/chat.addUser будет перенаправлено в метод addUser()

}
