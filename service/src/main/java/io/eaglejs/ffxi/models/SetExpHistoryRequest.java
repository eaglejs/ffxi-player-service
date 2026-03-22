package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetExpHistoryRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("expType")
    private Integer expType;

    @JsonProperty("points")
    private Integer points;

    @JsonProperty("chain")
    private Integer chain;

    @JsonProperty("timestamp")
    private String timestamp;

    // Getters and Setters
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Integer getExpType() { return expType; }
    public void setExpType(Integer expType) { this.expType = expType; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getChain() { return chain; }
    public void setChain(Integer chain) { this.chain = chain; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
