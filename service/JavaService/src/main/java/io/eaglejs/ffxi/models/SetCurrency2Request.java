package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetCurrency2Request {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("currency2")
    private Currency2 currency2;

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

    public Currency2 getCurrency2() {
        return currency2;
    }

    public void setCurrency2(Currency2 currency2) {
        this.currency2 = currency2;
    }
}
