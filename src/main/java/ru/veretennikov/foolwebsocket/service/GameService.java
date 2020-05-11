package ru.veretennikov.foolwebsocket.service;

import ru.veretennikov.foolwebsocket.model.GameContent;

import java.util.List;

public interface GameService {

    void addUser(String username, String userId);
    String removeUser(String username);

    boolean isGameStarted();
    void startGame(String sessionId);
    void processingCommand(String content, String userId);

    List<GameContent> getContent(String userId);

}
