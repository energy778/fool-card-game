package ru.veretennikov.foolwebsocket.service;

import ru.veretennikov.foolwebsocket.model.GameContent;

public interface GameService {

    void addUser(String username, String userId);
    String removeUser(String username);
    GameContent processingCommand(String content, String userId);

}
