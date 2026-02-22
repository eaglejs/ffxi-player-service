package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class SetBuffsRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("buffs")
    private Map<String, Integer> buffs;

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

    public Map<String, Integer> getBuffs() {
        return buffs;
    }

    public void setBuffs(Map<String, Integer> buffs) {
        this.buffs = buffs;
    }
}
