package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetCurrency2Request {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("domainPoints")
    private Integer domainPoints;

    @JsonProperty("eschaBeads")
    private Integer eschaBeads;

    @JsonProperty("eschaSilt")
    private Integer eschaSilt;

    @JsonProperty("gallantry")
    private Integer gallantry;

    @JsonProperty("gallimaufry")
    private Integer gallimaufry;

    @JsonProperty("hallmarks")
    private Integer hallmarks;

    @JsonProperty("mogSegments")
    private Integer mogSegments;

    @JsonProperty("mweyaPlasmCorpuscles")
    private Integer mweyaPlasmCorpuscles;

    @JsonProperty("potpourri")
    private Integer potpourri;

    @JsonProperty("coalitionImprimaturs")
    private Integer coalitionImprimaturs;

    @JsonProperty("temenosUnits")
    private Integer temenosUnits;

    @JsonProperty("apollyonUnits")
    private Integer apollyonUnits;

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

    public Integer getDomainPoints() { return domainPoints; }
    public void setDomainPoints(Integer domainPoints) { this.domainPoints = domainPoints; }

    public Integer getEschaBeads() { return eschaBeads; }
    public void setEschaBeads(Integer eschaBeads) { this.eschaBeads = eschaBeads; }

    public Integer getEschaSilt() { return eschaSilt; }
    public void setEschaSilt(Integer eschaSilt) { this.eschaSilt = eschaSilt; }

    public Integer getGallantry() { return gallantry; }
    public void setGallantry(Integer gallantry) { this.gallantry = gallantry; }

    public Integer getGallimaufry() { return gallimaufry; }
    public void setGallimaufry(Integer gallimaufry) { this.gallimaufry = gallimaufry; }

    public Integer getHallmarks() { return hallmarks; }
    public void setHallmarks(Integer hallmarks) { this.hallmarks = hallmarks; }

    public Integer getMogSegments() { return mogSegments; }
    public void setMogSegments(Integer mogSegments) { this.mogSegments = mogSegments; }

    public Integer getMweyaPlasmCorpuscles() { return mweyaPlasmCorpuscles; }
    public void setMweyaPlasmCorpuscles(Integer mweyaPlasmCorpuscles) { this.mweyaPlasmCorpuscles = mweyaPlasmCorpuscles; }

    public Integer getPotpourri() { return potpourri; }
    public void setPotpourri(Integer potpourri) { this.potpourri = potpourri; }

    public Integer getCoalitionImprimaturs() { return coalitionImprimaturs; }
    public void setCoalitionImprimaturs(Integer coalitionImprimaturs) { this.coalitionImprimaturs = coalitionImprimaturs; }

    public Integer getTemenosUnits() { return temenosUnits; }
    public void setTemenosUnits(Integer temenosUnits) { this.temenosUnits = temenosUnits; }

    public Integer getApollyonUnits() { return apollyonUnits; }
    public void setApollyonUnits(Integer apollyonUnits) { this.apollyonUnits = apollyonUnits; }
}
