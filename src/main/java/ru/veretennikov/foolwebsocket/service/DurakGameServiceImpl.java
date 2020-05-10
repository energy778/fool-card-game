package ru.veretennikov.foolwebsocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.veretennikov.foolwebsocket.common.util.CardDeckGenerator;
import ru.veretennikov.foolwebsocket.exception.DurakGameException;
import ru.veretennikov.foolwebsocket.exception.DurakGamePrivateException;
import ru.veretennikov.foolwebsocket.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DurakGameServiceImpl implements GameService {

    private final int MAX_NUM_CARD_ON_HAND = 6;

    private final int MIN_NUM_PLAYERS = 2;
    private final int MAX_NUM_PLAYERS = 6;

    private final Map<String, User> users = new HashMap();

    private boolean gameStarted;
    private CardDeck cardDeck;
    private Card reasonCard;
    private User curAttacker;
    private User curDefender;
    private User curSubattacker;
    private PlayerIterator playerIterator;

    // TODO: 011 11.05.20 нужны еще поля:
//        чей ход: сейчас ход защиты или нападения
//          защищаться можно только если сейчас ход защиты
//              а ход защиты до тех пор, пока есть на поле открытые пары
//        атаковать и подкидывать можно в любое время до тех пор, пока не будет 6 пар на поле


    @Override
    public boolean isGameStarted() {
        return this.gameStarted;
    }

    /**
     * список проверок, определяющих возможно ли начало новой игры, и запуск инициализации новой игры
     **/
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
        System.out.println(String.format("Список всех игроков: %s", users));        // отладить баг: java.util.ConcurrentModificationException: null

        if (UserRole.PLAYER.equals(user.getRole()))
            endGame();

        return username;

    }

    /**
     * выполнение хода
     **/
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
    public PublicGameContent getPublicContent(String userId) {
        return getStartPublicGameContent(userId);
    }


    /**
     * инициализация новой игры:
     *   инициализируем колоду: набор карт, перетасовка, определение козыря
     *   жеребьевка
     *   инициализация итератора
     *   определение ролей/типов всех остальных игроков
     **/
    private void initGame(String userId) {

        this.gameStarted = true;
        this.cardDeck = CardDeckGenerator.newCardDeck();
        toss();
        playerIterator = new PlayerIterator();  // итератор имеет смысл инициализировать только после определения атакующего: от него будет идти отсчет
        classification();

    }

    /**
     * жеребьевка: определение игроков из списка всех пользователей, карты, определяющей первый ход, игрока, делающего первый ход (атакующего)
     **/
    private void toss() {

        int i = 1;
        for (User user : users.values()) {
            user.setRole(UserRole.PLAYER);
            user.pickCards(this.cardDeck, MAX_NUM_CARD_ON_HAND);
            if (i == MAX_NUM_PLAYERS) {
                break;
            }
            i++;
        }

        reasonCard = users.values().stream()
                .filter(user -> UserRole.PLAYER.equals(user.getRole()))
                .flatMap(user -> user.getCards().stream())
                .reduce((card, card2) -> {
//                    жеребьевка: наименьшая козырная -> наименьшая некозырная -> любая
                    if (card.isTrump() && card2.isTrump())
                        return card.getRank().ordinal() < card2.getRank().ordinal() ? card : card2;
                    if (card.isTrump())
                        return card;
                    if (card2.isTrump())
                        return card2;
                    return card.getRank().ordinal() < card2.getRank().ordinal() ? card : card2;
                }).orElseThrow(() -> new DurakGameException("Ошибка жеребьёвки: не удалось определить карту, определяющую первый ход"));

        curAttacker = users.values().stream()
                .filter(user -> user.getCards().contains(reasonCard))
                .findFirst().orElseThrow(() -> new DurakGameException("Ошибка жеребьёвки: не удалось определить принадлежность карты, определяющей первый ход"));

        curAttacker.setPlayerType(PlayerType.ATTACKER);

    }

    /**
     * определение ролей/типов всех игроков по атакующему игроку и итератору
     *      предполагается вызывать эту функцию, когда определен атакующий и обнулены другие роли
     *      (не может быть два атакующих, поэтому все старые типы игроков логичнее затирать ПРИ ПОЛУЧЕНИИ НОВОГО АТАКУЮЩЕГО, в конце раунда)
     *         на атакующего указывает итератор
     *         следующий за ним будет защитник
     *         следующий - подкидывающий
     *         остальные - наблюдатели
     **/
    private void classification() {

        if (users.values().stream()
                .filter(user -> PlayerType.ATTACKER.equals(user.getPlayerType()))
                .count() != 1)
            throw new DurakGameException("Классификация игроков невозможна. Должен быть всего лишь один атакующий игрок");

        if (users.values().stream().anyMatch(user -> UserRole.PLAYER.equals(user.getRole())
                && !PlayerType.ATTACKER.equals(user.getPlayerType())
                && user.getPlayerType() != null))
            throw new DurakGameException("Классификация игроков невозможна. Типы всех игроков, кроме атакующего, должны быть очищены");

        curDefender = playerIterator.peekNextX(1);
        curDefender.setPlayerType(PlayerType.DEFENDER);

        curSubattacker = playerIterator.peekNextX(2);
        if (!curAttacker.equals(curSubattacker))
            curSubattacker.setPlayerType(PlayerType.SUBATTACKER);
        else
            curSubattacker = null;
//            если найденный подкидывающий и есть атакующий (играют всего два человека), то тип игрока не меняем

        users.forEach((s, user) -> {
//            для оптимизации из-за ленивой инициализации
            if (user.equals(curAttacker) || user.equals(curDefender) || user.equals(curSubattacker)) {
            } else
                user.setPlayerType(PlayerType.OBSERVER);
        });

    }

    private void endGame(){

        gameStarted = false;

//        выводим из игры игроков и очищаем карты в руках
        users.forEach((s, user) -> {
            user.setRole(UserRole.GUEST);
            user.setPlayerType(null);
            user.setHand(new Hand());
        });

//        очищаем колоду и прочие поля
        cardDeck = null;
        playerIterator = null;
        reasonCard = null;
        curAttacker = null;
        curDefender = null;
        curSubattacker = null;

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

    /**
     * получение публичного стартового игрового контента
     **/
    private PublicGameContent getStartPublicGameContent(String userId) {

//        нужно различать начало игры и уже непосредственный процесс

        PublicGameContent content = new PublicGameContent();

//        количество оставшихся в колоде карт
        content.setCardDeckSize(cardDeck.size());

//        козырная карта (в самом начале новой игры - чтобы отобразить ее один раз на игровом поле)
        content.setTrump(cardDeck.getTrump());

//        карта, которая победила в жеребьевке
        content.setReasonCard(reasonCard);

//        козырная масть
        content.setTrumpSuit(cardDeck.getTrumpSuit());

        content.setGameMessage(String.format("Игра началась. В колоде карт: %s. ", cardDeck.size())
                        + "\n Список участников: " + users.values().stream()
                                                        .filter(user -> UserRole.PLAYER.equals(user.getRole()))
                                                        .map(User::getName)
                                                        .sorted()
                                                        .collect(Collectors.joining(", "))
                        + "\n Ходит: " + curAttacker.getName()
                        + "\n Защищается: " + curDefender.getName()
                        + ((curSubattacker != null) ? "\n Подкидывает: " + curSubattacker.getName() : "")
        );

        return content;

    }


    /**
     * указатель устанавливается на первого атакующего игрока, который был определен ДО инициализации итератора (далее распределение ролей осуществляется после выбора игроков)
     * итератор предоставляет только функционал получения следующего игрока, а также взятия игрока без сдвига индекса с учетом пропуска определенного количества элементов
     **/
    private class PlayerIterator implements Iterator<User> {

        private final List<User> users = new ArrayList<>();
        private int index = 0;

        public PlayerIterator() {
            this.users.addAll(DurakGameServiceImpl.this.users.values().stream()
                                    .filter(user -> UserRole.PLAYER.equals(user.getRole()))
                                    .collect(Collectors.toList()));
            this.users.sort(Comparator.comparing(user -> !PlayerType.ATTACKER.equals(user.getPlayerType())));
        }

        @Override
        public boolean hasNext() {
            return getCountPlayers() >= 2;
        }

        @Override
        public User next() {

            if (!this.hasNext())
                throw new DurakGameException("Игра окончена. Остался один игрок с картами");

            while (true){
                this.index = (this.index + 1) % this.users.size();
                User user = this.users.get(this.index);
                if (UserRole.PLAYER.equals(user.getRole()))
                    return user;
            }

        }

        /**
         * получаем элемент с порядковым номером n, отсчитывая от текущего
         * функция вызывается, когда нужно пропустить игрока, который пропускает ход (взял карты)
         **/
        public User nextX(int n) {

            long countPlayers = getCountPlayers();
            if (n > countPlayers)
                throw new DurakGameException(String.format("В игре осталось игроков: %s. " +
                        "Не может быть осуществлён выбор заданного числа ходов (%s)",
                        countPlayers, n));

            for (int i = 0; i < n; i++) {
                this.next();
            }

            return this.users.get(this.index);

        }

        /**
         * (не трогая индекс) получаем элемент с порядковым номером n, отсчитывая от текущего
         **/
        public User peekNextX(int n) {

            long countPlayers = getCountPlayers();
            if (n > countPlayers)
                throw new DurakGameException(String.format("В игре осталось игроков: %s. " +
                                "Не может быть осуществлён выбор заданного числа ходов (%s)",
                        countPlayers, n));

            User user;
            int peekIndex = this.index;
            for (int i = 0; i < n; i++) {
                do {
                    peekIndex = (peekIndex + 1) % this.users.size();
                    user = this.users.get(peekIndex);
                } while (!UserRole.PLAYER.equals(user.getRole()));
            }

            return this.users.get(peekIndex);

        }

        private long getCountPlayers() {
            return this.users.stream()
                    .filter(user -> UserRole.PLAYER.equals(user.getRole()))     // потому что роли пользователей в процессе игры меняются
                    .count();
        }

    }

}
