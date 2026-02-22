package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetCurrency1Request {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("currency1")
    private Currency1 currency1;

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Currency1 getCurrency1() {
        return currency1;
    }

    public void setCurrency1(Currency1 currency1) {
        this.currency1 = currency1;
    }
}
