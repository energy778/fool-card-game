package ru.veretennikov.foolwebsocket.exception;

public class DurakGamePrivateException extends GamePrivateException {
    public DurakGamePrivateException(String message, String sessionId) {
        super(message, sessionId);
    }
}
