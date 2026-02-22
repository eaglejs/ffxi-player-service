package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class SetStatsRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("masterLevel")
    private Integer masterLevel;

    @JsonProperty("mainJobLevel")
    private Integer mainJobLevel;

    @JsonProperty("subJobLevel")
    private Integer subJobLevel;

    @JsonProperty("attack")
    private Integer attack;

    @JsonProperty("defense")
    private Integer defense;

    @JsonProperty("title")
    private String title;

    @JsonProperty("nationRank")
    private Integer nationRank;

    @JsonProperty("currentExemplar")
    private Integer currentExemplar;

    @JsonProperty("requiredExemplar")
    private Integer requiredExemplar;

    @JsonUnwrapped
    private Stats stats;

    // Getters and Setters
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Integer getMasterLevel() { return masterLevel; }
    public void setMasterLevel(Integer masterLevel) { this.masterLevel = masterLevel; }

    public Integer getMainJobLevel() { return mainJobLevel; }
    public void setMainJobLevel(Integer mainJobLevel) { this.mainJobLevel = mainJobLevel; }

    public Integer getSubJobLevel() { return subJobLevel; }
    public void setSubJobLevel(Integer subJobLevel) { this.subJobLevel = subJobLevel; }

    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) { this.attack = attack; }

    public Integer getDefense() { return defense; }
    public void setDefense(Integer defense) { this.defense = defense; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getNationRank() { return nationRank; }
    public void setNationRank(Integer nationRank) { this.nationRank = nationRank; }

    public Integer getCurrentExemplar() { return currentExemplar; }
    public void setCurrentExemplar(Integer currentExemplar) { this.currentExemplar = currentExemplar; }

    public Integer getRequiredExemplar() { return requiredExemplar; }
    public void setRequiredExemplar(Integer requiredExemplar) { this.requiredExemplar = requiredExemplar; }

    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
}
