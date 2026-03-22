package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetCapacityPointsRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("numberOfJobPoints")
    private Integer total;

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

    public Integer getNumberOfJobPoints() {
        return total;
    }

    public void setNumberOfJobPoints(Integer numberOfJobPoints) {
        this.total = numberOfJobPoints;
    }
}
