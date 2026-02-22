package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshBuffsRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

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
}
