package ru.veretennikov.foolwebsocket.controller;

import ru.veretennikov.foolwebsocket.model.Card;
import ru.veretennikov.foolwebsocket.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import ru.veretennikov.foolwebsocket.model.ChatMessageSimple;
import ru.veretennikov.foolwebsocket.model.GameContent;
import ru.veretennikov.foolwebsocket.util.CardGenerate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessageSimple message) {

        ChatMessage newMessage = new ChatMessage();
        newMessage.setSender(message.getSender());
        newMessage.setType(ChatMessage.MessageType.CHAT);

        GameContent content = new GameContent();
//        String enteredCard = message.getContent();        надо генерировать карту на основе присланной информации
        content.setCards(generateCards());
        newMessage.setContent(content);

        return newMessage;

    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

//    все сообщения от клиентов, направленные по адресу, начинающемуся с /app, будут перенаправлены в соответствующие методы
//    Имелись в виду как раз методы, аннотированные @MessageMapping
//    Например, сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage()
//    А например, сообщение, направленное по адресу/app/chat.addUser будет перенаправлено в метод addUser()

    private List<Card> generateCards() {
        Random random = new Random();
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < random.nextInt(4) + 1; i++) {
            cards.add(CardGenerate.getNewCard());
        }
        return cards;
    }

}
