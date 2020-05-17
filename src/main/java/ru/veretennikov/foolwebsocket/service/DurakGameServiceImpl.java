package ru.veretennikov.foolwebsocket.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.veretennikov.foolwebsocket.common.util.CardDeckGenerator;
import ru.veretennikov.foolwebsocket.exception.DurakGameException;
import ru.veretennikov.foolwebsocket.exception.DurakGamePrivateException;
import ru.veretennikov.foolwebsocket.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static ru.veretennikov.foolwebsocket.model.DurakGameEvent.*;
import static ru.veretennikov.foolwebsocket.model.PlayerType.*;
import static ru.veretennikov.foolwebsocket.model.Rank.*;
import static ru.veretennikov.foolwebsocket.model.UserRole.GUEST;
import static ru.veretennikov.foolwebsocket.model.UserRole.PLAYER;

@Slf4j
@Service
@NoArgsConstructor
public class DurakGameServiceImpl implements GameService {

    private final int MAX_NUM_CARD_IN_HAND = 6;
    private final int MAX_NUM_CARD_ON_FIELD = 6;

    private final int MIN_NUM_PLAYERS = 2;
    private final int MAX_NUM_PLAYERS = 6;

    private final List<Rank> ranks = Arrays.asList(SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE);
    private final Map<String, User> users = new HashMap();

    private boolean gameStarted;
    private boolean roundBegun;
    private int round;
    private DurakGameEvent curEvent;
    private Field field;
    private CardDeck cardDeck;
    private Card reasonCard;                // карта, которая определила первого ходящего
    private Card curCard;                   // карта, которой сделали ход
    private PlayerIterator playerIterator;

    private User curWinner;
    private User curPlayer;     // атакующий или защитник, ход которого ожидается в настоящий момент
    private User curAttacker;
    private User curDefender;
    private User curSubattacker;

    @Override
    public void addUser(String username, String userId) {

        User newUser = new User(userId, username);
        newUser.setRole(GUEST);
        users.put(userId, newUser);

        System.out.println(String.format("%s вошел в игру", username));
        System.out.println(String.format("Список всех игроков: %s", users));

    }

    @Override
    public String removeUser(String userId) {

        User user = users.remove(userId);
        String username = user.getName();

        System.out.println(String.format("%s вышел из игры", username));
        System.out.println(String.format("Список всех игроков: %s", users));        // отладить редкий баг: java.util.ConcurrentModificationException: null

        if (PLAYER.equals(user.getRole())){
            curEvent = USER_OUT;
            curPlayer = user;
            endGame();
        }

        return username;

    }

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

    /**
     * анализ пришедшей команды
     **/
    @Override
    public void checkCommand(String message, String userId) {

        log.debug(String.format("Incoming message. id: %s, message: %s", userId, message));

        curEvent = NO_GAME;
        this.curCard = null;
        this.curWinner = null;

        User initiator = users.get(userId);
        if (initiator == null)
            throw new DurakGameException("Не удалось идентифицировать пользователя, приславшего сообщение");
        if (!PLAYER.equals(initiator.getRole()))
            throw new DurakGamePrivateException("В данный момент игра уже идёт. Дождитесь ее окончания и присоединяйтесь к новой игре. Спасибо", userId);

        PlayerType initiatorPlayerType = initiator.getPlayerType();
        if (FINISHER.equals(initiatorPlayerType))
            throw new DurakGamePrivateException("Вы не можете сделать ход, так как закончили игру. Дождитесь ее окончания и присоединяйтесь к новой", userId);
        if (OBSERVER.equals(initiatorPlayerType)
                || DEFENDER.equals(initiatorPlayerType) && !curPlayer.equals(curDefender)
                || SUBATTACKER.equals(initiatorPlayerType) && initiator.equals(curSubattacker) && !roundBegun)
//                || ATTACKER.equals(initiatorPlayerType) && !curPlayer.equals(curAttacker)      // комм.: атакующий может подкидывать и далее
            throw new DurakGamePrivateException("Вы не можете сделать ход, дождитесь своей очереди", userId);

        int index;

        try {
            index = Integer.parseInt(message);
            if (index < 0)
                throw new DurakGamePrivateException("Некорректный ввод. Введено отрицательное число", userId);
        } catch (NumberFormatException e) {
            throw new DurakGamePrivateException("Некорректный ввод. Введите порядковый номер карты, начиная с 1 или 0 (опционально)", userId);
        }

        Pair curOpenPair = null;

        if (initiator.equals(curSubattacker)){
//            пытаемся подкинуть карты

            if (index == 0)
                throw new DurakGamePrivateException("Некорректный ввод. Подкидывающий может только подкидывать карты. Введите порядковый номер карты, начиная с 1", userId);

            checkSubattack(userId, initiator, index);
            curEvent = SUBATT_STEP;

        } else if (initiator.equals(curAttacker)){
//            пытаемся атаковать

            if (roundBegun){

                if (index == 0){

                    if (field.getOpenPairs().size() != 0)
                        throw new DurakGamePrivateException("Вы не можете завершить раунд, на поле еще есть открытые пары", userId);

                    curEvent = DEF_PASS;

                } else {

                    checkSubattack(userId, initiator, index);
                    curEvent = ATT_STEP;

                }

            } else {

                if (index > initiator.getCards().size())
                    throw new DurakGamePrivateException("Некорректный ввод. Введите порядковый номер карты, начиная с 1", userId);
                if (index == 0)
                    throw new DurakGamePrivateException("Вы не можете пасовать в начале атаки. Введите порядковый номер карты, начиная с 1", userId);

                curEvent = ATT_STEP;

            }

        } else if (initiator.equals(curDefender)){
//            пытаемся защититься

            List<Pair> openPairs = field.getOpenPairs();
            if (openPairs.size() == 0)
                throw new DurakGamePrivateException("На поле нет открытых пар. Сейчас ход атаки", userId);  // никогда не ожидаемо

            if (index == 0){

                curEvent = DEF_FALL;

            } else {
//                отбиваемся

                if (index > initiator.getCards().size())
                    throw new DurakGamePrivateException("Некорректный ввод. Введите порядковый номер карты, начиная с 1", userId);

                Card cardDefender = initiator.getCards().get(index - 1);
                boolean step = false;

                for (Pair openPair : openPairs) {
                    Card cardAttacker = openPair.getAttacker();

                    // сначала отбивайся некозырными :)
                    if (cardDefender.isTrump() && !cardAttacker.isTrump()
                            || cardDefender.getSuit() == cardAttacker.getSuit() && cardDefender.getRank().ordinal() > cardAttacker.getRank().ordinal()){
                        curOpenPair = openPair;
                        step = true;
                        break;
                    }
                }

                if (!step)
                    throw new DurakGamePrivateException("Вы не можете отбить выбранной картой ни одну карту на поле. Введите порядковый номер карты, начиная с 1 или 0 для получения карт с поля", userId);

                curEvent = DEF_STEP;

            }

        } else {
            throw new DurakGameException(String.format("Не удалось идентифицировать пользователя, приславшего сообщение: %s", initiator));

        }

        if (!NO_GAME.equals(curEvent))
            processingStep(userId, initiator, index, curOpenPair);

    }

    @Override
    public List<GameContent> getContent(String userId) {

        ArrayList<GameContent> content = new ArrayList<>();

        if (gameStarted && round == 1 && field.getPairs().isEmpty()) {
//            начало игры

//            начало нового раунда
            addContentNewRound(content);

            DurakPublicStartGameContent publicContent;

//            карта, определившая первый ход
            publicContent = new DurakPublicStartGameContent();
            publicContent.setGameEvent(ANNOUNCE_REASON_CARD);
            publicContent.setReasonCard(reasonCard);
            publicContent.setGameMessage("Карта, определившая первый ход: ");
            content.add(publicContent);

//            приват. карты игроков
            List<PrivateGameContent> privateContent = users.values().stream()
                    .filter(user -> PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()))
                    .map(user -> getCurrentPrivateGameContent(user.getId()))
                    .collect(Collectors.toList());
            content.addAll(privateContent);

//            приват. 'ваш ход'
            addPrivateContentStep(content);

            return content;

        } else {

            if (curEvent == null || NO_GAME.equals(curEvent))
                return Collections.emptyList();

            if (gameStarted){
//                идёт игра

                if (roundBegun){
//                    ход

                    DurakPublicCurrentGameContent publicContent = new DurakPublicCurrentGameContent();
                    publicContent.setCardStep(curCard);
                    publicContent.setGameEvent(curEvent);
                    publicContent.setGameMessage(users.get(userId).getName());
                    content.add(publicContent);

//                    новый состав карт для ходившего
                    if (curWinner == null)
                        content.add(getCurrentPrivateGameContent(userId));

                } else {
//                    окончание раунда и начало нового

//                    инфа о результатах предыдущего раунда
                    DurakPublicCurrentGameContent publicContentAfter = new DurakPublicCurrentGameContent();
                    publicContentAfter.setGameEvent(curEvent);
                    String eventString;
                    if (DEF_PASS.equals(curEvent))
                        eventString = "бито";
                    else if (DEF_FALL.equals(curEvent))
                        eventString = "защита взяла карты";
                    else if (DEF_STEP.equals(curEvent))
                        eventString = "защита вышла из игры";
                    else
                        throw new DurakGameException(String.format("Не удалось определить текстовое представление события окончания раунда: %s", curEvent));
                    publicContentAfter.setGameMessage(String.format("Раунд завершён: %s. ", eventString));
                    content.add(publicContentAfter);

//                    инфа о новом раунде
                    addContentNewRound(content);

//                    обновление информации о картах на руках игроков
                    List<PrivateGameContent> privateContents = users.values().stream()
                            .filter(user -> PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()))
                            .map(user -> getCurrentPrivateGameContent(user.getId()))
                            .collect(Collectors.toList());
                    content.addAll(privateContents);

                }

//                "Ваш ход"
                addPrivateContentStep(content);

            } else {
//                Игра завершена

                DurakPublicCurrentGameContent endContent = new DurakPublicCurrentGameContent();
                endContent.setGameEvent(curEvent);
                if (curPlayer == null)
                    endContent.setGameMessage("Игра завершена, ничья");
                else if (USER_OUT.equals(curEvent))
                    endContent.setGameMessage(String.format("Игра завершена, %s сдался", curPlayer.getName()));
                else
                    endContent.setGameMessage(String.format("Игра завершена, проиграл %s", curPlayer.getName()));
                content.add(endContent);

            }

        }

        return content;

    }



    /**
     * инициализация новой игры:
     *   инициализируем колоду: набор карт, перетасовка, определение козыря
     *   жеребьевка
     *   инициализация итератора
     *   определение ролей/типов всех остальных игроков
     **/
    private void initGame(String userId) {

        curEvent = null;
        reasonCard = null;
        curCard = null;
        curPlayer = null;
        curAttacker = null;
        curDefender = null;
        curSubattacker = null;
        curWinner = null;

        this.gameStarted = true;
        this.cardDeck = CardDeckGenerator.newCardDeck(ranks);
        toss();
        playerIterator = new PlayerIterator();  // итератор имеет смысл инициализировать только после определения атакующего: от него будет идти отсчет
        classification();
        roundBegun = false;
        round = 1;
        field = new Field();

    }

    /**
     * жеребьевка: определение игроков из списка всех пользователей, карты, определяющей первый ход, игрока, делающего первый ход (атакующего)
     **/
    private void toss() {

        int i = 1;
        for (User user : users.values()) {
            user.setRole(PLAYER);
            user.pickCards(this.cardDeck, MAX_NUM_CARD_IN_HAND);
            if (i == MAX_NUM_PLAYERS) {
                break;
            }
            i++;
        }

        reasonCard = users.values().stream()
                .filter(user -> PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()))
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

        curAttacker.setPlayerType(ATTACKER);
        curPlayer = curAttacker;

    }

    /**
     * определение ролей/типов всех игроков по атакующему игроку и итератору
     *      предполагается вызывать эту функцию, когда определен атакующий и обнулены другие роли
     *         на атакующего указывает итератор
     *         следующий за ним будет защитник
     *         следующий - подкидывающий
     *         остальные - наблюдатели
     **/
    private void classification() {

        if (users.values().stream()
                .filter(user -> ATTACKER.equals(user.getPlayerType()))
                .count() != 1)
            throw new DurakGameException("Классификация игроков невозможна. Должен быть всего лишь один атакующий игрок");

        if (users.values().stream().anyMatch(user -> PLAYER.equals(user.getRole())
                && !ATTACKER.equals(user.getPlayerType())
                && !FINISHER.equals(user.getPlayerType())
                && user.getPlayerType() != null))
            throw new DurakGameException("Классификация игроков невозможна. Типы всех игроков, кроме атакующего и завершивших игру, должны быть очищены");

        curDefender = playerIterator.peekNextX(1);
        curDefender.setPlayerType(DEFENDER);

        curSubattacker = playerIterator.peekNextX(2);
        if (!curAttacker.equals(curSubattacker))
            curSubattacker.setPlayerType(SUBATTACKER);
        else
            curSubattacker = null;
//            если найденный подкидывающий и есть атакующий (играют всего два человека), то тип игрока не меняем

        users.forEach((s, user) -> {
//            для оптимизации (ленивая инициализация)
            if (!(PLAYER.equals(user.getRole()))
                    || user.equals(curAttacker)
                    || user.equals(curDefender)
                    || user.equals(curSubattacker)
                    || FINISHER.equals(user.getPlayerType())) {
            } else
                user.setPlayerType(OBSERVER);
        });

    }

    /**
     * выполнение хода
     **/
    private void processingStep(String userId, User initiator, int index, Pair openPair) {

        curWinner = null;
        System.out.println(curEvent);
        if (index != 0)
            this.curCard = initiator.getCards().get(index - 1);

//        у вышедших игроков меняем роли сразу
        switch (curEvent) {

            case NO_GAME:
                return;     // никогда не ожидаемо

            case DEF_PASS:
                roundUp();
                fetchCards();
                break;

            case DEF_FALL:
                curDefender.pickCards(field.fetchAll());
                roundUp();
                fetchCards();
                break;

            case DEF_STEP:
                if (openPair == null)
                    throw new DurakGamePrivateException("Не найдена открытая пара для отбивания карты", userId);        // никогда не ожидаема
                field.closePair(openPair, initiator.getCards().remove(index - 1));
                curWinner = checkUserWin(curDefender);
                if (curWinner != null) {
                    curDefender = null;
                    roundUp();
                    fetchCards();
                }
                break;

            case ATT_STEP:
                field.openPair(new Pair(initiator.getCards().remove(index - 1)));
                roundBegun = true;
                curWinner = checkUserWin(curAttacker);
                if (curWinner != null)
                    curAttacker = null;
                break;

            case SUBATT_STEP:
                field.openPair(new Pair(initiator.getCards().remove(index - 1)));
                curWinner = checkUserWin(curSubattacker);
                if (curWinner != null)
                    curSubattacker = null;
                break;

            default:
                throw new DurakGameException("Возникло необрабатываемое исключение: не определен тип события игры");

        }

//        не закончена ли игра?
        if ((cardDeck.isEmpty() && getCountCardInHands() == 0)
                || loserFound() ){
//            ничья
            curEvent = GAME_END;
            endGame();
            return;
        }

//        роли нужно переопределять только если закончился раунд
//        а ходящих - всегда
        if (!roundBegun){
//            раунд завершился и сбросили флаг

            if (DEF_FALL.equals(curEvent)){
                curAttacker = playerIterator.nextX(2);
            } else
                curAttacker = playerIterator.next();

            curAttacker.setPlayerType(ATTACKER);
            curPlayer = curAttacker;
            users.forEach((s, user) -> {
                if (PLAYER.equals(user.getRole()) && !user.equals(curAttacker) && !FINISHER.equals(user.getPlayerType()))
                    user.setPlayerType(null);
            });
            classification();

        } else {
//            определение нового ходящего

//            если остались неотбитые карты, то
//                  неважно, кто ходил - сейчас надо дать возможность защитнику отбиться
//                  также если не осталось никого в атаке, то ход защитника, чтобы он мог завершить раунд
            if (!field.getOpenPairs().isEmpty() || (curAttacker == null && curSubattacker == null))
//                переклассификация не требуется. могла смениться очередь ходящего, но роли не менялись
                curPlayer = curDefender;
            else if (curAttacker != null)
                curPlayer = curAttacker;
            else {
//                атакующий вышел. смена атакующего
                curSubattacker.setPlayerType(ATTACKER);
                curPlayer = curSubattacker;
                curAttacker = curSubattacker;
                curSubattacker = null;
            }

        }

    }

    /**
     * проверка возможности подкинуть карту
     **/
    private void checkSubattack(String userId, User initiator, int index) {

        if (field.getPairs().size() > (MAX_NUM_CARD_ON_FIELD - 1))
            throw new DurakGamePrivateException(String.format("Вы не можете сделать ход, допускается только %s пар на игровом поле", MAX_NUM_CARD_ON_FIELD), userId);

        if (index > initiator.getCards().size())
            throw new DurakGamePrivateException("Некорректный ввод. Введите порядковый номер карты, начиная с 1", userId);

        if (!field.getPlayedRanks().contains(initiator.getCards().get(index - 1).getRank()))
            throw new DurakGamePrivateException("Вы не можете подкинуть карту, которой нет на игровом поле", userId);

        if ((Math.min(MAX_NUM_CARD_ON_FIELD, curDefender.getCards().size()) - field.getOpenPairs().size()) < 1)
            throw new DurakGamePrivateException("Вы не можете подкинуть карту, так как у защищающегося игрока не хватит карт, чтобы отбиться", userId);

    }

    /**
     * добор карт игроками, которые еще не вышли из игры
     **/
    private void fetchCards() {
        users.forEach((s, user) -> {
            if (PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()))
                user.pickCards(cardDeck, MAX_NUM_CARD_IN_HAND);
        });
    }

    /**
     * получение всех карт на руках всех пользователей
     **/
    private long getCountCardInHands() {
        return users.values().stream().map(User::getCards).count();
    }

    /**
     * переход на новый раунд
     **/
    private void roundUp() {
        round++;
        roundBegun = false;
        field = new Field();
    }

    /**
     * проверка, должен ли выйти игрок
     **/
    private User checkUserWin(User curUser) {
        if (cardDeck.isEmpty() && curUser.getCards().isEmpty()){
            curUser.setPlayerType(FINISHER);
            return curUser;
        } else
            return null;
    }

    /**
     * проверка на наличие проигравшего
     **/
    private boolean loserFound() {

        if (!cardDeck.isEmpty())
            return false;

//        if (playerIterator.hasNext())     не анализирует количество карт на руках
        if (users.values().stream().filter(user -> !user.getCards().isEmpty()).count() > 1)
            return false;

        if (curPlayer.equals(curDefender) && curPlayer.getCards().size() == 1 && field.getOpenPairs().size() == 1)
            return false;

        curPlayer = users.values().stream()
                .filter(user -> !user.getCards().isEmpty()).findFirst().orElseThrow(() -> new DurakGameException("Не удается определить проигравшего"));

        return true;

    }

    private void endGame(){

        gameStarted = false;
        round = 0;

//        выводим из игры игроков и очищаем карты в руках
        users.forEach((s, user) -> {
            user.setRole(GUEST);
            user.setPlayerType(null);
            user.setHand(new Hand());
        });

    }


    private void addContentNewRound(ArrayList<GameContent> content) {

//        объявление игроков
        DurakPublicStartGameContent publicContent = new DurakPublicStartGameContent();
        publicContent.setGameEvent(ROUND_BEGIN);
        StringBuilder sb = new StringBuilder("Раунд %s начался. ");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(round));
        sb.append(System.lineSeparator());
        if (cardDeck.size() > 0){
            sb.append("В колоде карт: %s. ");
            args.add(String.valueOf(cardDeck.size()));
        } else {
            sb.append("В колоде не осталось карт. ");
        }
        sb.append(System.lineSeparator());
        sb.append("Список участников: %s ");
        args.add(playerIterator.getUsers());
        sb.append(System.lineSeparator());
        sb.append("Ходит: %s. ");
        args.add(curAttacker.getName());
        sb.append(System.lineSeparator());
        sb.append("Защищается: %s. ");
        args.add(curDefender.getName());
        if (curSubattacker != null) {
            sb.append(System.lineSeparator());
            sb.append("Подкидывает: %s. ");
            args.add(curSubattacker.getName());
        }
        publicContent.setGameMessage(String.format(sb.toString(), args.toArray()));
        content.add(publicContent);

//        козырная карта
        publicContent = new DurakPublicStartGameContent();
        publicContent.setGameEvent(ANNOUNCE_TRUMP);
        publicContent.setTrump(cardDeck.getTrump());
        publicContent.setGameMessage("Козырная карта: ");
        content.add(publicContent);

    }

    /**
     * личное сообщение участнику о его картах
     **/
    private PrivateGameContent getCurrentPrivateGameContent(String userId) {

        PrivateGameContent content = new PrivateGameContent();
        content.setUserId(userId);
        content.setGameMessage("Ваши карты (эту строку видите только вы): ");
        content.setCards(users.get(userId).getCards());
        return content;

    }

    /**
     * личные сообщения с предложением сделать ход/подкинуть карту
     **/
    private void addPrivateContentStep(ArrayList<GameContent> content) {

        PrivateGameContent privateStartGameContent;

        privateStartGameContent = new PrivateGameContent();
        privateStartGameContent.setUserId(curPlayer.getId());
        String gameMessage = "Ваш ход. Введите цифру от 1 до " + curPlayer.getCards().size();
        if (curPlayer == curAttacker)
            gameMessage += " или 0 для паса";
        if (curPlayer == curDefender)
            gameMessage += " или 0 для взятия карт";
        privateStartGameContent.setGameMessage(gameMessage);
        content.add(privateStartGameContent);

        if (roundBegun && curSubattacker != null){
            privateStartGameContent = new PrivateGameContent();
            privateStartGameContent.setUserId(curSubattacker.getId());
            privateStartGameContent.setGameMessage("Вы можете подкинуть карту");
            content.add(privateStartGameContent);
        }

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
                                    .filter(user -> PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()))
                                    .collect(Collectors.toList()));
            this.users.sort(Comparator.comparing(user -> !ATTACKER.equals(user.getPlayerType())));
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
                if (PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()))
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
                } while (!PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType()));
            }

            return this.users.get(peekIndex);

        }

        private long getCountPlayers() {
            return this.users.stream()
                    .filter(user -> (PLAYER.equals(user.getRole()) && !FINISHER.equals(user.getPlayerType())))     // потому что роли пользователей в процессе игры меняются
                    .count();
        }

        /**
         * возвращает игроков в том порядке, в котором они расположены в игре
         **/
        public String getUsers() {

            StringBuilder sb = new StringBuilder();
            int oldIndex = index;
            int size = this.users.size();
            do {
                sb.append(users.get(oldIndex).getName());
                sb.append(", ");
                oldIndex = (oldIndex + 1) % size;
            } while (oldIndex != index);

            return sb.toString();

        }

    }

}
