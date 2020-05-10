package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {

    private String id;
    private String name;
    private UserRole role;

    private PlayerType playerType;
    private Hand hand;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.hand = new Hand();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", hand=" + hand +
                '}';
    }

    public void pickCards(CardDeck cardDeck, int maxNumCardOnHand) {
        int need = Math.min(cardDeck.size(), maxNumCardOnHand - hand.getCards().size());
        this.hand.getCards().addAll(cardDeck.getSomeCards(need));
    }

    public List<Card> getCards() {
        return hand.getCards();
    }

}
