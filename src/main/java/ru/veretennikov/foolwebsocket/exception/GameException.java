package ru.veretennikov.foolwebsocket.exception;

public abstract class GameException extends RuntimeException{
    public GameException(String message){
        super(message);
    }
}
