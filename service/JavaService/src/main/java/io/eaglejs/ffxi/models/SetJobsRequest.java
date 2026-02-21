package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetJobsRequest {

    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("mainJob")
    private String mainJob;

    @JsonProperty("subJob")
    private String subJob;

    public SetJobsRequest() {
    }

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

    public String getMainJob() {
        return mainJob;
    }

    public void setMainJob(String mainJob) {
        this.mainJob = mainJob;
    }

    public String getSubJob() {
        return subJob;
    }

    public void setSubJob(String subJob) {
        this.subJob = subJob;
    }
}
