package ru.veretennikov.foolwebsocket.service;

import ru.veretennikov.foolwebsocket.model.GameContent;

public interface GameService {

    void addUser(String username, String sessionId);
    String removeUser(String username);

    // TODO: 006 06.05.20 дорабатывать
    GameContent getContent();

    // TODO: 006 06.05.20 временно
    void checkNewGame(String content);

}
