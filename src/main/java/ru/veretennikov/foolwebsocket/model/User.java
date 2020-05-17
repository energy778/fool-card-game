package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
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

//    добираем из колоды и укладываем в отсортированном порядке
    public void pickCards(CardDeck cardDeck, int maxNumCardOnHand) {
        int need = Math.min(cardDeck.size(), maxNumCardOnHand - hand.getCards().size());
        this.hand.getCards().addAll(cardDeck.getSomeCards(need));
        this.hand.getCards().sort(Comparator.comparing(card -> (((Card) card).isTrump()))
                                        .thenComparing(card -> ((Card) card).getRank().ordinal())
                                        .thenComparing(card -> ((Card) card).getSuit()));
    }

//    берём с поля все карты и укладываем в отсортированном порядке
    public void pickCards(List<Card> cards) {
        this.hand.getCards().addAll(cards);
        this.hand.getCards().sort(Comparator.comparing(card -> (((Card) card).isTrump()))
                .thenComparing(card -> ((Card) card).getRank().ordinal())
                .thenComparing(card -> ((Card) card).getSuit()));
    }

    public List<Card> getCards() {
        return hand.getCards();
    }

}
