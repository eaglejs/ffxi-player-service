package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SetBuffsRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("buffs")
    private List<Integer> buffs;

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

    public List<Integer> getBuffs() {
        return buffs;
    }

    public void setBuffs(List<Integer> buffs) {
        this.buffs = buffs;
    }
}
