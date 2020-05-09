package ru.veretennikov.foolwebsocket.exception;

import lombok.Getter;

@Getter
public abstract class PrivateException extends RuntimeException{
    private String sessionId;
    public PrivateException(String message, String sessionId){
        super(message);
        this.sessionId = sessionId;
    }
}
