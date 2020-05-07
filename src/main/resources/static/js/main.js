'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var sessionId = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

var colorsSuit = [
    '#f00', '#000', '#000', '#f00'
];

var suits = [
    '♥', '♠', '♣', '♦'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {
            sessionId = socket.sessionId;
            onConnected(frame);
        }, onError);

    }
    event.preventDefault();
}


function onConnected(options) {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

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
    var messageContent = messageInput.value.trim();

    if(messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' покинул нас';
    } else if (message.type === 'CHAT') {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);
        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    if (message.type === 'CHAT') {

        if (message.gameContent.message === null){

            let cards = message.gameContent.cards;
            // alert(message.message);
            for (var i = 0; i < message.content.cards.length; i++){

                var card = cards[i];
                var suitIndex = card.suit;
                var suit = suits[suitIndex];

                var cardElement = document.createElement('g-card');

                var cardElementText1 = document.createElement('div');
                var cardText1 = document.createTextNode(suit);
                cardElementText1.appendChild(cardText1);

                var cardElementText2 = document.createElement('div');
                var cardText2 = document.createTextNode(card.rank);
                cardElementText2.appendChild(cardText2);

                var cardElementText3 = document.createElement('div');
                var cardText3 = document.createTextNode(suit);
                cardElementText3.appendChild(cardText3);

                cardElement.style['color'] = colorsSuit[suitIndex];
                if (card.trump)
                    cardElement.style['font-weight'] = 'bold';

                cardElement.appendChild(cardElementText1);
                cardElement.appendChild(cardElementText2);
                cardElement.appendChild(cardElementText3);

                // контейнер для карт
                var cElementContainer = document.createElement('span');
                cElementContainer.appendChild(cardElement);
                messageElement.appendChild(cElementContainer);

                // TODO: можно сделать так, чтобы карты выводились на следующей строке после ника

            }

        } else {

            var textElement = document.createElement('p');
            var messageText = document.createTextNode(message.gameContent.message);
            textElement.appendChild(messageText);
            messageElement.appendChild(textElement);

        }

    } else {

        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);

    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;

}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
