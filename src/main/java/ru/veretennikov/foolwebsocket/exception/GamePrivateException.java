package ru.veretennikov.foolwebsocket.exception;

public abstract class GamePrivateException extends PrivateException{
    public GamePrivateException(String message, String sessionId){
        super(message, sessionId);
    }
}
