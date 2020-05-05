package ru.veretennikov.foolwebsocket.controller;

import lombok.extern.slf4j.Slf4j;
import ru.veretennikov.foolwebsocket.model.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import ru.veretennikov.foolwebsocket.util.CardGenerate;

@Slf4j
@Controller
public class GameController {

    private static CardDeck cardDeck = CardGenerate.newCardDeck();

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessageSimple message) {

//        генерация новой колоды. пока что через "особое" сообщение по текущему же адресу (только при условии, что в колоде уже не осталось карт)
        if ("new".equalsIgnoreCase(message.getContent()) && cardDeck.isEmpty())
            cardDeck = CardGenerate.newCardDeck();

        ChatMessage newMessage = new ChatMessage();
        newMessage.setSender(message.getSender());
        newMessage.setType(ChatMessage.MessageType.CHAT);

        GameContent content = new GameContent();
//        String enteredCard = message.getContent();        надо генерировать карту на основе присланной информации
        content.setCards(cardDeck.getSomeCards(4));
        newMessage.setContent(content);

        // TODO: 004 04.05.20 починить сраный логгер
        log.debug("Осталось в колоде: {}", cardDeck.size());

        return newMessage;

    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

//    все сообщения от клиентов, направленные по адресу, начинающемуся с /app, будут перенаправлены в соответствующие методы
//    Имелись в виду как раз методы, аннотированные @MessageMapping
//    Например, сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage()
//    А например, сообщение, направленное по адресу/app/chat.addUser будет перенаправлено в метод addUser()

}
