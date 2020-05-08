package ru.veretennikov.foolwebsocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.veretennikov.foolwebsocket.common.util.CardGenerate;
import ru.veretennikov.foolwebsocket.exception.DurakGameException;
import ru.veretennikov.foolwebsocket.model.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DurakGameServiceImpl implements GameService {

    private final int MAX_NUM_CARD_ON_HAND = 6;

    private final int MIN_NUM_PLAYERS = 2;
    private final int MAX_NUM_PLAYERS = 6;

    private boolean gameStarted;
    private CardDeck cardDeck;

    private CardDeck initCardDeck() {
        return CardGenerate.newCardDeck();
    }

    private final Map<String, User> users = new HashMap();

    /**
     * получение реального игрового контента по пользователю
     **/
    private GameContent getCurrentGameContent(String userId) {

        GameContent content = new GameContent();

        // TODO: 007 07.05.20 добавить проверки
        content.setCards(users.get(userId).getCards());

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
    public void addUser(String username, String userId) {

        User newUser = new User(userId, username);
        newUser.setRole(UserRole.GUEST);
        users.put(userId, newUser);

        System.out.println(String.format("%s вошел в игру", username));
        System.out.println(String.format("Список всех игроков: %s", users));

    }

    @Override
    public String removeUser(String userId) {

        User user = users.remove(userId);
        String username = user.getName();

        System.out.println(String.format("%s вышел из игры", username));
        System.out.println(String.format("Список всех игроков: %s", users));

// TODO: 006 06.05.20 заканчивать игру, если пользователь был игроком, а не наблюдателем
//        endGame();

        return username;

    }

    @Override
    public GameContent processingCommand(String message, String userId) {

//        фиктивный игровой контент
        GameContent content = new GameContent();

        if (this.gameStarted){

//        делают ход (нападают, подкидывают, отбиваются, говорят "бито", говорят "пас")
//        пытаются написать во время игры (не будучи игроком, не в свою очередь, не той картой)

            // TODO: 007 07.05.20 пытаемся обработать сообщение как игровое
//            но не забываем, что могут прислать всякую муру. такие сообщения (не индексы. просто игнорим)
//            также тут нужны все проверки типа его ли ход и вот это вот всё
            throw new DurakGameException("Попытка выполнить ход будет добавлена в следующих версиях");

        } else if ("go".equalsIgnoreCase(message)){

//            попытка начать игру

            if (users.size() < MIN_NUM_PLAYERS) {
                // TODO: 008 08.05.20 сейчас выводится от участника, но не должно. можно придумать другой тип сообщения
                //          но игровой сервис не должен знать вообще, что есть какие-то типы сообщений
                throw new DurakGameException("Минимальное количество игроков для игры - 2. " +
                        "Игра против компьютера пока не поддерживается");
            }

            initGame();

            return getCurrentGameContent(userId);

        } else {

//            просто болтают
            content.setMessage(message);

        }

        return content;

    }

    private void initGame() {

        this.gameStarted = true;

//        инициализируем колоду: весь набор карт, перетасовка, определение козыря
        this.cardDeck = initCardDeck();

//        выбираем игроков из списка пользователей
        int i = 1;
        for (User user : users.values()) {
            user.setRole(UserRole.PLAYER);
            user.pickCards(this.cardDeck, MAX_NUM_CARD_ON_HAND);
            if (i == MAX_NUM_PLAYERS) {
                break;
            }
            i++;
        }

        // TODO: 007 07.05.20 жеребьевка и объявление результатов

    }

    private void endGame(){

        gameStarted = false;

//        выводим из игры игроков и очищаем карты в руках
        users.forEach((s, user) -> {
            user.setRole(UserRole.GUEST);
            user.setHand(new Hand());
        });

//        очищаем колоду
        cardDeck = null;

    }

}
