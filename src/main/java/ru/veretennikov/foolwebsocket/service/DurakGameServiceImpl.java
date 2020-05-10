package ru.veretennikov.foolwebsocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.veretennikov.foolwebsocket.common.util.CardDeckGenerator;
import ru.veretennikov.foolwebsocket.exception.DurakGamePrivateException;
import ru.veretennikov.foolwebsocket.model.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DurakGameServiceImpl implements GameService {

    private final int MAX_NUM_CARD_ON_HAND = 6;

    private final int MIN_NUM_PLAYERS = 2;
    private final int MAX_NUM_PLAYERS = 6;

    private boolean gameStarted;
    private CardDeck cardDeck;

    private void initCardDeck() {
        this.cardDeck = CardDeckGenerator.newCardDeck();
    }

    private final Map<String, User> users = new HashMap();

    @Override
    public boolean isGameStarted() {
        return this.gameStarted;
    }

    @Override
    public void startGame(String userId) {

        if (users.size() < MIN_NUM_PLAYERS) {
            throw new DurakGamePrivateException("Минимальное количество игроков для игры - 2. Игра против компьютера пока не поддерживается", userId);
        }

        initGame(userId);

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

        if (UserRole.PLAYER.equals(user.getRole()))
            endGame();

        return username;

    }

    @Override
    public void processingCommand(String message, String userId) {

        // TODO: 007 07.05.20 пытаемся обработать сообщение как игровое
//            но не забываем, что могут прислать всякую муру. такие сообщения (не индексы. просто игнорим)
//            также тут нужны все проверки типа его ли ход и вот это вот всё

//        получаем карты на руках чувака
//        получаем его роль на данный момент
//        получаем свойство, чей сейчас ход
//        анализируем присланное сообщение: либо индексы либо 0 (пасс/бита) если есть такая возможность
//        иначе на хер

        throw new DurakGamePrivateException("Попытка выполнить ход будет добавлена в следующих версиях", userId);

    }

    @Override
    public Map<String, PrivateGameContent> getPrivateContent() {
//        return users.entrySet().stream()
//                .filter(entry -> UserRole.PLAYER.equals(entry.getValue().getRole()))
//                .collect(
//                        HashMap::new,
//                        (map, entry) -> map.put(entry.getKey(), getCurrentPrivateGameContent(entry.getKey())),
//                        HashMap::putAll
//                );
        return users.values().stream()
                .filter(user -> UserRole.PLAYER.equals(user.getRole()))
                .collect(Collectors.toMap(User::getId, user -> getCurrentPrivateGameContent(user.getId())));

    }

    @Override
    public PublicGameContent getPublicContent(String sessionId) {
        return getStartPublicGameContent(sessionId);
    }

    private void initGame(String userId) {

        this.gameStarted = true;

//        инициализируем колоду: весь набор карт, перетасовка, определение козыря
        initCardDeck();

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
//        или через сервис или создать внутренний класс итератор

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

    /**
     * получение публичного игрового контента
     **/
    private PublicGameContent getStartPublicGameContent(String sessionId) {

//        нужно различать начало игры и уже непосредственный процесс

        PublicGameContent content = new PublicGameContent();

//        // TODO: 007 07.05.20 добавить проверки
//        content.setCards(cardDeck.getCards());

//        количество оставшихся в колоде карт
        content.setCardDeckSize(cardDeck.size());

//        козырная карта (в самом начале новой игры - чтобы отобразить ее один раз на игровом поле)
        content.setTrump(cardDeck.getTrump());

//        козырь
        content.setTrumpSuit(cardDeck.getTrumpSuit());

        content.setGameMessage(String.format("Игра началась. В колоде карт: %s. Козырь: ", cardDeck.size())
                        + "\nСписок участников: " + users.values().stream()
                                                        .filter(user -> UserRole.PLAYER.equals(user.getRole()))
                                                        .map(User::getName)
                                                        .collect(Collectors.joining(", "))
                );

//        чей ход
//        сейчас ход защиты или нападения
//          защищаться можно только если сейчас ход защиты
//              а ход защиты до тех пор, пока есть на поле открытые пары
//        атаковать и подкидывать можно в любое время до тех пор, пока не будет 6 пар на поле

        // TODO: 006 06.05.20 на время отладки
        System.out.println(String.format("Возвращаемый контент: %s", content));

        return content;

    }

    /**
     * получение приватного игрового контента пользователя
     **/
    private PrivateGameContent getCurrentPrivateGameContent(String userId) {

        PrivateGameContent content = new PrivateGameContent();
        content.setCards(users.get(userId).getCards());
        content.setGameMessage("Ваши карты (эту строку видите только вы): ");
        return content;

    }

}
