package ru.veretennikov.foolwebsocket.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Field {

    private ArrayList<Pair> pairs;
    private ArrayList<Rank> playedRanks;

    public Field() {
        this.pairs = new ArrayList<>();
        this.playedRanks = new ArrayList<>();
    }

    /**
     * работать с парами необходимо через эту функцию, чтобы обновлялся список "играющих" карт
     **/
    public void addPair(Pair pair){

        this.pairs.add(pair);
        Rank rankAttacker = pair.getAttacker().getRank();
        if (!playedRanks.contains(rankAttacker))
            playedRanks.add(rankAttacker);

    }

    public List<Pair> getOpenPairs() {

        return pairs.stream()
                .filter(pair -> pair.getDefender() == null)
                .collect(Collectors.toList());

    }

    public List<Card> fetchAll() {

        ArrayList<Card> cards = pairs.stream()
                .flatMap(pair -> Stream.of(pair.getAttacker(), pair.getDefender()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        pairs = new ArrayList<>();
        playedRanks = new ArrayList<>();

        return cards;

    }

}
