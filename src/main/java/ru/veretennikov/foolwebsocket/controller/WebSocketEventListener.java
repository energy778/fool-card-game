package ru.veretennikov.foolwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.veretennikov.foolwebsocket.core.model.ChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ClientChatMessage;
import ru.veretennikov.foolwebsocket.core.model.ServerChatMessage;
import ru.veretennikov.foolwebsocket.service.GameService;
import ru.veretennikov.foolwebsocket.service.SendService;

import static ru.veretennikov.foolwebsocket.core.model.ChatMessage.MessageType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final GameService gameService;
    private final SendService sendService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Received a new web socket connection. Session id: {}", StompHeaderAccessor.wrap(event.getMessage()).getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = headerAccessor.getSessionId();

        if (userId != null) {

            String username = gameService.removeUser(userId);
            log.info("User Disconnected : " + username);

            ServerChatMessage chatMessage = new ServerChatMessage();
            chatMessage.setType(LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/events", chatMessage);

            ClientChatMessage clientChatMessage = new ClientChatMessage();
            clientChatMessage.setSender(username);
//            clientChatMessage.setType(LEAVE);
            sendService.sendMessages(clientChatMessage, GAME_MESSAGE, gameService.getContent(userId));

        }

    }

}
