package ru.veretennikov.foolwebsocket.model;

public enum DurakGameEvent {

    NO_GAME,

    ROUND_BEGIN,
    ANNOUNCE_TRUMP,
    ANNOUNCE_REASON_CARD,

    DEF_STEP,
    ATT_STEP,
    SUBATT_STEP,

    DEF_PASS,
    DEF_FALL,

    USER_OUT,
    GAME_END

}
