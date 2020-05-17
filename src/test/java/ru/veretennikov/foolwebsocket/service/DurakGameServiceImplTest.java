package ru.veretennikov.foolwebsocket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.veretennikov.foolwebsocket.exception.DurakGameException;
import ru.veretennikov.foolwebsocket.exception.DurakGamePrivateException;
import ru.veretennikov.foolwebsocket.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.veretennikov.foolwebsocket.model.PlayerType.*;
import static ru.veretennikov.foolwebsocket.model.Rank.*;
import static ru.veretennikov.foolwebsocket.model.UserRole.GUEST;
import static ru.veretennikov.foolwebsocket.model.UserRole.PLAYER;

class DurakGameServiceImplTest {

    private DurakGameServiceImpl gameService;

    @BeforeEach
    public void init(){

        gameService = new DurakGameServiceImpl();

//        Map<String, User> users = (Map<String, User>) ReflectionTestUtils.getField(gameService, "users");
//        assertNotNull(users);
//
//        ReflectionTestUtils.setField(gameService, "gameStarted", false);
//        ReflectionTestUtils.setField(gameService, "round", 0);
//        ReflectionTestUtils.setField(gameService, "roundBegun", false);
//        ReflectionTestUtils.setField(gameService, "curEvent", NO_GAME);
//        ReflectionTestUtils.setField(gameService, "field", new Field());
//        ReflectionTestUtils.setField(gameService, "cardDeck", CardDeckGenerator.newCardDeck(Arrays.asList(SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE)));
//        ReflectionTestUtils.setField(gameService, "reasonCard", null);
//        ReflectionTestUtils.setField(gameService, "playerIterator", null);
//        ReflectionTestUtils.setField(gameService, "curCard", null);
//        ReflectionTestUtils.setField(gameService, "curPlayer", null);
//        ReflectionTestUtils.setField(gameService, "curAttacker", null);
//        ReflectionTestUtils.setField(gameService, "curDefender", null);
//        ReflectionTestUtils.setField(gameService, "curSubattacker", null);
//        ReflectionTestUtils.setField(gameService, "curWinner", null);

    }

    @Test
    void addUser() {

        Map<String, User> users = (Map<String, User>) ReflectionTestUtils.getField(gameService, "users");
        assertNotNull(users);
        int be4 = users.size();
        gameService.addUser("user", "1");
        assertEquals(users.size(), be4 + 1);
        assertEquals(GUEST, users.get("1").getRole());

    }

    @Test
    void removeUser() {

        Map<String, User> users = (Map<String, User>) ReflectionTestUtils.getField(gameService, "users");
        assertNotNull(users);
        User userDefault = new User("1", "user");
        users.put("1", userDefault);
        int be4 = users.size();
        String removeUser = gameService.removeUser("1");
        assertNotNull(removeUser);
        assertEquals(removeUser, userDefault.getName());
        assertEquals(users.size(), be4 -1);

    }

    @Test
    void removeUserEndGame(){

        Map<String, User> users = (Map<String, User>) ReflectionTestUtils.getField(gameService, "users");
        assertNotNull(users);

        ReflectionTestUtils.setField(gameService, "gameStarted", true);

        User userDefault = new User("1", "user");
        userDefault.setRole(GUEST);
        users.put("1", userDefault);

        User playerDefault = new User("2", "player");
        playerDefault.setRole(PLAYER);
        users.put("2", playerDefault);

        gameService.removeUser("1");
        assertTrue(((boolean) ReflectionTestUtils.getField(gameService, "gameStarted")));
        gameService.removeUser("2");
        assertTrue(!((boolean) ReflectionTestUtils.getField(gameService, "gameStarted")));

    }

    @Test
    void startGame() {

        gameService = new DurakGameServiceImpl();
        assertThrows(DurakGamePrivateException.class, () -> gameService.startGame("1"));

        Map<String, User> users = (Map<String, User>) ReflectionTestUtils.getField(gameService, "users");
        assertNotNull(users);
        int maxNumPlayers = (int) ReflectionTestUtils.getField(gameService, "MAX_NUM_PLAYERS");
        int maxNumCardInHand = (int) ReflectionTestUtils.getField(gameService, "MAX_NUM_CARD_IN_HAND");
        addSomeUsers(users, maxNumPlayers +2, 0);

        gameService.startGame("1");
        assertTrue(((boolean) ReflectionTestUtils.getField(gameService, "gameStarted")));

        assertEquals(false, ReflectionTestUtils.getField(gameService, "roundBegun"));
        assertEquals(1, ((int) ReflectionTestUtils.getField(gameService, "round")));
        assertNotNull(ReflectionTestUtils.getField(gameService, "field"));
        assertNotNull(ReflectionTestUtils.getField(gameService, "cardDeck"));
        assertNotNull(ReflectionTestUtils.getField(gameService, "playerIterator"));
        assertNull(ReflectionTestUtils.getField(gameService, "curEvent"));
        assertNull(ReflectionTestUtils.getField(gameService, "curCard"));
        assertNull(ReflectionTestUtils.getField(gameService, "curWinner"));

        assertEquals(maxNumPlayers +2, users.size());
        assertEquals(maxNumPlayers, users.values().stream().filter(user -> PLAYER.equals(user.getRole())).count());
        assertEquals(maxNumPlayers * maxNumCardInHand,
                users.values().stream()
                        .filter(user -> PLAYER.equals(user.getRole()))
                        .mapToLong(user -> user.getCards().size())
                        .sum());

        Card reasonCard = (Card) ReflectionTestUtils.getField(gameService, "reasonCard");
        assertNotNull(reasonCard);
        assertNotNull(reasonCard.getRank());
        assertTrue(reasonCard.getSuit() >= 0 || reasonCard.getSuit() < 4);

        User curPlayer = (User) ReflectionTestUtils.getField(gameService, "curPlayer");
        assertNotNull(curPlayer);
        User curAttacker = (User) ReflectionTestUtils.getField(gameService, "curAttacker");
        assertNotNull(curAttacker);
        assertEquals(curPlayer, curAttacker);
        assertEquals(PLAYER, curAttacker.getRole());
        assertEquals(ATTACKER, curAttacker.getPlayerType());

        assertEquals(1, users.values().stream()
                .filter(user -> ATTACKER.equals(user.getPlayerType()))
                .count());
        assertEquals(1, users.values().stream()
                .filter(user -> DEFENDER.equals(user.getPlayerType()))
                .count());
        assertEquals(1, users.values().stream()
                .filter(user -> SUBATTACKER.equals(user.getPlayerType()))
                .count());
        assertEquals(maxNumPlayers -1 -1 -1, users.values().stream()
                .filter(user -> OBSERVER.equals(user.getPlayerType()))
                .count());

        assertNotNull(ReflectionTestUtils.getField(gameService, "curDefender"));
        assertNotNull(ReflectionTestUtils.getField(gameService, "curSubattacker"));

    }

    @Test
    void checkCommandWithExceptions() {

        gameService = new DurakGameServiceImpl();
        ReflectionTestUtils.setField(gameService, "gameStarted", true);

        Map<String, User> users = (Map<String, User>) ReflectionTestUtils.getField(gameService, "users");
        assertNotNull(users);
        addSomeUsers(users, 3, 3);      // первые три пользователя - гости, вторые три пользователя - игроки
        String incomeMessageTemplate = "hi";

//        сообщение от пользователя с неизвестным id
        assertThrows(DurakGameException.class, () -> gameService.checkCommand(incomeMessageTemplate, "-1"));
//        сообщение от пользователя с ролью, отличной от PLAYER
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(incomeMessageTemplate, "1"));
//        сообщение от пользователя, который закончил игру (вышел из игры)
        users.get("3").setPlayerType(FINISHER);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(incomeMessageTemplate, "3"));
//        сообщение от пользователя с типом игрока - наблюдатель
        users.get("3").setPlayerType(OBSERVER);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(incomeMessageTemplate, "3"));
//        сообщение от защитника в то время, когда ход атаки
        users.get("3").setPlayerType(DEFENDER);
        ReflectionTestUtils.setField(gameService, "curPlayer", users.get("3"));
        ReflectionTestUtils.setField(gameService, "curDefender", users.get("4"));
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(incomeMessageTemplate, "3"));
//        сообщение от подкидывающего в то время, когда раунд еще не начался (на игровом поле нет карт)
        users.get("3").setPlayerType(SUBATTACKER);
        ReflectionTestUtils.setField(gameService, "curPlayer", users.get("3"));
        ReflectionTestUtils.setField(gameService, "curSubattacker", users.get("3"));
        ReflectionTestUtils.setField(gameService, "roundBegun", false);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(incomeMessageTemplate, "3"));

        users.get("3").setPlayerType(DEFENDER);
        ReflectionTestUtils.setField(gameService, "curPlayer", users.get("3"));
        ReflectionTestUtils.setField(gameService, "curDefender", users.get("3"));
//        на данный момент попытка сделать ход защитником в свою очередь

//        ввели текст в качестве сообщения
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(incomeMessageTemplate, "3"));
//        передали отрицательное число
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("-1", "3"));
//        передали null в качестве сообщения
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand(null, "3"));

        users.get("3").setPlayerType(SUBATTACKER);
        ReflectionTestUtils.setField(gameService, "curSubattacker", users.get("3"));
        ReflectionTestUtils.setField(gameService, "roundBegun", true);
//        на данный момент у нас попытка сделать ход подкидывающим

//        подкидывающий

//        подкидывающий не может взять карты или сказать 'пас'
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("0", "3"));
//        попытка подкинуть карту, когда на игровом поле уже достигнуто максимальное количество пар
        int maxNumCardOnField = (int) ReflectionTestUtils.getField(gameService, "MAX_NUM_CARD_ON_FIELD");
        Field field = new Field();
        List<Pair> pairs = Stream.generate(() -> new Pair(new Card())).limit(maxNumCardOnField).collect(Collectors.toList());
        ReflectionTestUtils.setField(field, "pairs", pairs);
        ReflectionTestUtils.setField(gameService, "field", field);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "3"));
//        введенный индекс карты выходит за границы массива карт на руках у игрока
        pairs.remove(0);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "3"));
//        попытка подкинуть карту, которой нет на игровом поле (такого же ранга)
        ArrayList<Rank> playedRanks = new ArrayList<>(Collections.singletonList(ACE));
        ReflectionTestUtils.setField(field, "playedRanks", playedRanks);
        Card attackerOnHand = new Card();
        attackerOnHand.setRank(KING);
        users.get("3").getCards().add(attackerOnHand);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "3"));
//        попытка подкинуть карту, когда у пользователя недостаточно карт для ее отбивания
        List<Card> attackerCards = users.get("3").getCards();
        attackerCards.clear();
        Card attacker = new Card();
        attacker.setRank(ACE);
        attackerCards.add(attacker);
        User curDefender = users.get("4");
        ReflectionTestUtils.setField(gameService, "curDefender", curDefender);
        pairs.clear();
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "3"));
//        curEvent = SUBATT_STEP;
//        замокать процессинг идея так себе - но проверить бы выбираемое событие не помешало - может быть позже - через процессинг. но нужны такие же условия

//        атакующий

        User curAttacker = users.get("5");
        curAttacker.setPlayerType(ATTACKER);
        ReflectionTestUtils.setField(gameService, "curAttacker", curAttacker);
        ReflectionTestUtils.setField(gameService, "curPlayer", curAttacker);

//        попытка сделать пас/сказать "бито", когда на игровом поле еще не все карты покрыты
        ReflectionTestUtils.setField(gameService, "roundBegun", true);
        pairs = Stream.generate(() -> new Pair(new Card())).limit(1).collect(Collectors.toList());
        ReflectionTestUtils.setField(field, "pairs", pairs);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("0", "5"));

//        другие случаи протестированы выше для подкидывающего
        ReflectionTestUtils.setField(gameService, "roundBegun", false);

//        индекс выбранной карты выходит за размер массива карт в руке игрока
        List<Card> cardsAttacker = users.get("5").getCards();
        cardsAttacker.clear();
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "5"));
//        попытка сделать пас/сказать "бито" в начале атаки
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("0", "5"));

//        защитник

        curDefender = users.get("4");
        curDefender.setPlayerType(DEFENDER);
        ReflectionTestUtils.setField(gameService, "curDefender", curDefender);
        ReflectionTestUtils.setField(gameService, "curPlayer", curDefender);
        pairs = new ArrayList<>();
        ReflectionTestUtils.setField(field, "pairs", pairs);
//        попытка выполнить ход защитой в свою очередь, когда на поле нет открытых пар
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("0", "4"));
//        индекс выбранной карты выходит за размер массива карт в руке игрока
        Card attackerCard = new Card();
        attackerCard.setRank(SEVEN);
        attackerCard.setSuit(0);
        attackerCard.setTrump(true);
        pairs.add(new Pair(attackerCard));
        ReflectionTestUtils.setField(field, "pairs", pairs);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("117", "4"));
//        пользователь не может отбить карту на игровом поле
        Card defenderCard = new Card();
        defenderCard.setRank(SIX);
        defenderCard.setSuit(1);
        defenderCard.setTrump(false);
        List<Card> defenderCards = curDefender.getCards();
        defenderCards.add(defenderCard);
//        некозырной козырную
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "4"));
//        картой меньшего достоинства карту большего достоинства
        defenderCard.setSuit(0);
        attackerCard.setTrump(false);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "4"));
//        некозырной картой некозырную карту другой масти
        defenderCard.setSuit(1);
        defenderCard.setRank(EIGHT);
        assertThrows(DurakGamePrivateException.class, () -> gameService.checkCommand("1", "4"));

//        случай, когда игрок, делающий ход, не определен ни как текущий нападающий, ни как защитник, ни как подкидывающий
        ReflectionTestUtils.setField(gameService, "curSubattacker", null);
        ReflectionTestUtils.setField(gameService, "curAttacker", null);
        ReflectionTestUtils.setField(gameService, "curDefender", null);

    }

//    для итератора нужен отдельный тест
//    для остальных методов - лучше написать интеграционные тесты


    private void addSomeUsers(Map<String, User> users, int numGuests, int numPlayers) {

        int numUsers = 0;

        for (int i = 0; i < numGuests; i++, ++numUsers) {
            String guestId = String.valueOf(numUsers);
            User guest = new User(guestId, String.format("guest %s", numUsers));
            guest.setRole(GUEST);
            users.put(guestId, guest);
        }

        for (int i = 0; i < numPlayers; i++, ++numUsers) {
            String playerId = String.valueOf(numUsers);
            User player = new User(playerId, String.format("player %s", numUsers));
            player.setRole(PLAYER);
            users.put(playerId, player);
        }

    }

}
