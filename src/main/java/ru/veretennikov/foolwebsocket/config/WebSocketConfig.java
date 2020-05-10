package ru.veretennikov.foolwebsocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
    * регистрируем конечную точку, которую клиенты будут использовать, чтобы подключиться к нашему Websocket-серверу.
    * SockJS – для браузеров, которые не поддерживают Websocket.
    * Зачем нужен Stomp? Дело в том, что сам по себе WebSocket не дает таких вещей (более высокого уровня)
    * как отправка сообщений пользователям, подписанным на тему, или отправка сообщений конкретному пользователю
    */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry config) {
        config.addEndpoint("/ws").withSockJS();
    }

    /**
     * настраиваем брокер сообщений, который будет использоваться для направления сообщений от одного клиента к другому
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

//        сообщения, чей адрес (куда отправлены) начинается с  “/app“, должны быть направлены в методы, занимающиеся обработкой сообщений (аннотированные @MessageMapping)
        config.setApplicationDestinationPrefixes("/app");

//        сообщения, чей адрес начинается с  “/topic“, должны быть направлены в брокер сообщений
//        Брокер перенаправляет сообщения всем клиентам, подписанным на тему.
        config.enableSimpleBroker("/topic");   // Enables a simple in-memory broker

    }

}
