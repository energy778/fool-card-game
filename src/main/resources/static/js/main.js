'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');

const colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];
const colorsSuit = [
    '#f00', '#000', '#000', '#f00'
];
const suits = [
    '♥', '♠', '♣', '♦'
];

let stompClient = null;
let username = null;
let sessionId = null;

// app

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        let socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {
            sessionId = socket.sessionId;
            console.log('sessionId: ' + sessionId);
            onConnected(frame);
        }, onError);

    }
    event.preventDefault();
}

function onConnected(options) {

    stompClient.subscribe('/topic/events', onUserEvent);
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/private/' + sessionId, onPrivateMessageReceived);
    // stompClient.subscribe('/user/' + sessionId + '/topic/private/', onPrivateMessageReceived);
    stompClient.subscribe('/topic/game/errors/' + sessionId, onGameExceptionMessageReceived);
    stompClient.subscribe('/topic/game/errors', onGameExceptionMessageReceived);
    stompClient.subscribe('/topic/errors/' + sessionId, onExceptionMessageReceived);
    stompClient.subscribe('/topic/errors', onExceptionMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    );

    connectingElement.classList.add('hidden');

}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    let messageContent = messageInput.value.trim();

    if(messageContent && stompClient) {
        let chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'MESSAGE'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

// общие ошибки приложения
function onExceptionMessageReceived(payload) {
    // String
    showMessageInChat(payload.body);
}

// события, связанные (пока) со входом/выходом пользователей
function onUserEvent(payload) {
    // ServerChatMessage

    let message = JSON.parse(payload.body);

    if(message.type === 'JOIN') {
        // текст приветствия сформирован на сервере
    } else if (message.type === 'LEAVE') {
        message.content = message.sender + ' покинул нас';
    } else {
        return;
    }

    showMessageInChat(message.content);

}

// ошибки, связанные с игрой
function onGameExceptionMessageReceived(payload) {
    // String
    showMessageInChat(payload.body);
}

// личные сообщения
function onPrivateMessageReceived(payload) {

    let message = JSON.parse(payload.body);
    let messageElement = document.createElement('li');

    messageElement.classList.add('chat-message');

    if(message.type === 'START_GAME') {
        let cards = message.gameContent.cards;
        showCards(cards, messageElement);
    } else if(message.type === 'GAME_MESSAGE') {
        // nothing yet
    } else {
        return;
    }

    let textElement = document.createElement('p');
    let messageText = document.createTextNode(message.gameContent.gameMessage);
    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;

}

// все остальные сообщения
function onMessageReceived(payload) {

    let message = JSON.parse(payload.body);
    let messageElement = document.createElement('li');

    if (message.type === 'MESSAGE') {
        // просто сообщение в чат. всегда от имени пользователя

        messageElement.classList.add('chat-message');

        let avatarElement = document.createElement('i');
        let avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);
        messageElement.appendChild(avatarElement);

        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);

        //
        let textElement = document.createElement('p');
        let messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);

    } else if (message.type === 'START_GAME') {
        // игровое сообщение. начало игры

        messageElement.classList.add('chat-message');

        let textElement;
        let messageText;

        textElement = document.createElement('span');      // p
        messageText = document.createTextNode(message.gameContent.gameMessage);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);

        textElement = document.createElement('span');
        messageText = document.createTextNode("Козырная карта: ");
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
        showCards(Array.of(message.gameContent.trump), messageElement);

        textElement = document.createElement('span');
        messageText = document.createTextNode("Карта, определившая первый ход");
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
        showCards(Array.of(message.gameContent.reasonCard), messageElement);

    } else if (message.type === 'GAME_MESSAGE') {
        // игровое сообщение. может быть от пользователя

        messageElement.classList.add('chat-message');

        let avatarElement = document.createElement('i');
        let avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);
        messageElement.appendChild(avatarElement);

        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);

        showCards(message.gameContent.cards, messageElement);

    } else {
        return;
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;

}


// service

function showMessageInChat(message) {

    let messageElement = document.createElement('li');
    messageElement.classList.add('event-message');

    let textElement = document.createElement('p');
    let messageText = document.createTextNode(message);
    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;

}

function getAvatarColor(messageSender) {

    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    let index = Math.abs(hash % colors.length);

    return colors[index];

}

function showCards(cards, messageElement) {

    if (cards === null)
        return;

    for (let i = 0; i < cards.length; i++) {

        let card = cards[i];
        let suitIndex = card.suit;
        let suit = suits[suitIndex];

        let cardElement = document.createElement('g-card');

        let cardElementText1 = document.createElement('div');
        let cardText1 = document.createTextNode(suit);
        cardElementText1.appendChild(cardText1);

        let cardElementText2 = document.createElement('div');
        let cardText2 = document.createTextNode(card.rank);
        cardElementText2.appendChild(cardText2);

        let cardElementText3 = document.createElement('div');
        let cardText3 = document.createTextNode(suit);
        cardElementText3.appendChild(cardText3);

        cardElement.style['color'] = colorsSuit[suitIndex];
        if (card.trump)
            cardElement.style['font-weight'] = 'bold';

        cardElement.appendChild(cardElementText1);
        cardElement.appendChild(cardElementText2);
        cardElement.appendChild(cardElementText3);

        // контейнер для карт
        let cElementContainer = document.createElement('span');
        cElementContainer.appendChild(cardElement);
        messageElement.appendChild(cElementContainer);

        // TODO: можно сделать так, чтобы карты выводились на следующей строке после ника

    }

}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
