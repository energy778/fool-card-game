package ru.veretennikov.foolwebsocket.controller;

import lombok.extern.slf4j.Slf4j;
import ru.veretennikov.foolwebsocket.model.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import ru.veretennikov.foolwebsocket.util.CardGenerate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
@Controller
public class ChatController {

    private static CardDeck cardDeck = CardGenerate.getCardDeck();

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessageSimple message) {

//        генерация новой колоды. пока что через "особое" сообщение по текущему же адресу (только при условии, что в колоде уже не осталось карт)
        if ("new".equalsIgnoreCase(message.getContent()) && cardDeck.getCards().isEmpty())
            cardDeck = CardGenerate.getCardDeck();

        ChatMessage newMessage = new ChatMessage();
        newMessage.setSender(message.getSender());
        newMessage.setType(ChatMessage.MessageType.CHAT);

        GameContent content = new GameContent();
//        String enteredCard = message.getContent();        надо генерировать карту на основе присланной информации
        content.setCards(generateCards());
        newMessage.setContent(content);

        // TODO: 004 04.05.20 починить сраный логгер
        log.debug("Осталось в колоде: {}", cardDeck.getCards().size());

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

    private List<Card> generateCards() {

        Random random = new Random();
        int curTrumpSuit = cardDeck.getTrumpSuit();

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < random.nextInt(4) + 1; i++) {
            if (cardDeck.getCards().isEmpty())
                break;
            Card curCard = cardDeck.getCards().pop();
            curCard.setTrump(curCard.getSuit() == curTrumpSuit);
            cards.add(curCard);
        }

        cards.sort(Comparator.comparing(Card::getRank));

        return cards;

    }

}
