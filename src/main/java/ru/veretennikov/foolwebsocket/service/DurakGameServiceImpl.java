package ru.veretennikov.foolwebsocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.veretennikov.foolwebsocket.common.util.CardGenerate;
import ru.veretennikov.foolwebsocket.model.CardDeck;
import ru.veretennikov.foolwebsocket.model.GameContent;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DurakGameServiceImpl implements GameService {

    // TODO: 005 05.05.20 может быть имеет смысл избавиться от content-а и размещать все объекты прямо здесь
    private CardDeck cardDeck = CardGenerate.newCardDeck();
    private final List<String> users = new ArrayList<>();

    @Override
    public GameContent getContent() {

        GameContent content = new GameContent();

//        нужна информация о картах на руках пользователя
        content.setCards(cardDeck.getSomeCards(4));     // тупо берем произвольное количество карт (от 1 до 4)
//        количество оставшихся в колоде карт
        content.setCardDeckSize(cardDeck.size());
//        козырь
        content.setTrumpSuit(cardDeck.getTrumpSuit());
//        иногда - козырная карта (в самом начале новой игры - чтобы отобразить ее один раз на игровом поле)
        content.setTrump(cardDeck.getTrump());

        // TODO: 006 06.05.20 на время отладки
//        log.debug("Возвращаемый контент: {}", content);
        System.out.println(String.format("Возвращаемый контент: %s", content));
        return content;

    }

    @Override
    public void addUser(String username) {
        users.add(username);
        System.out.println(String.format("%s вошел в игру", username));
        System.out.println(String.format("Список всех игроков: %s", users));
    }

    @Override
    public void removeUser(String username) {
        // TODO: 006 06.05.20 заканчивать игру, если пользователь был игроком, а не наблюдателем
        users.remove(username);
        System.out.println(String.format("%s вышел из игры", username));
        System.out.println(String.format("Список всех игроков: %s", users));
    }

    @Override
    public void checkNewGame(String message) {
        if ("new".equalsIgnoreCase(message) && cardDeck.isEmpty())
            cardDeck = CardGenerate.newCardDeck();
    }

}

