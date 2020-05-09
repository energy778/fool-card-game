package ru.veretennikov.foolwebsocket.service;

import ru.veretennikov.foolwebsocket.model.PrivateGameContent;
import ru.veretennikov.foolwebsocket.model.PublicGameContent;

import java.util.Map;

public interface GameService {

    void addUser(String username, String userId);
    String removeUser(String username);

    boolean isGameStarted();
    void startGame();
    void processingCommand(String content, String userId);
    Map<String, PrivateGameContent> getPrivateContent();
    PublicGameContent getPublicContent();

}
