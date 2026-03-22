package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetCurrency1Request {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("conquestPointsBastok")
    private Integer conquestPointsBastok;

    @JsonProperty("conquestPointsSandoria")
    private Integer conquestPointsSandoria;

    @JsonProperty("conquestPointsWindurst")
    private Integer conquestPointsWindurst;

    @JsonProperty("deeds")
    private Integer deeds;

    @JsonProperty("dominionNotes")
    private Integer dominionNotes;

    @JsonProperty("imperialStanding")
    private Integer imperialStanding;

    @JsonProperty("loginPoints")
    private Integer loginPoints;

    @JsonProperty("nyzulTokens")
    private Integer nyzulTokens;

    @JsonProperty("sparksOfEminence")
    private Integer sparksOfEminence;

    @JsonProperty("therionIchor")
    private Integer therionIchor;

    @JsonProperty("unityAccolades")
    private Integer unityAccolades;

    @JsonProperty("voidstones")
    private Integer voidstones;

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

    public Integer getConquestPointsBastok() { return conquestPointsBastok; }
    public void setConquestPointsBastok(Integer conquestPointsBastok) { this.conquestPointsBastok = conquestPointsBastok; }

    public Integer getConquestPointsSandoria() { return conquestPointsSandoria; }
    public void setConquestPointsSandoria(Integer conquestPointsSandoria) { this.conquestPointsSandoria = conquestPointsSandoria; }

    public Integer getConquestPointsWindurst() { return conquestPointsWindurst; }
    public void setConquestPointsWindurst(Integer conquestPointsWindurst) { this.conquestPointsWindurst = conquestPointsWindurst; }

    public Integer getDeeds() { return deeds; }
    public void setDeeds(Integer deeds) { this.deeds = deeds; }

    public Integer getDominionNotes() { return dominionNotes; }
    public void setDominionNotes(Integer dominionNotes) { this.dominionNotes = dominionNotes; }

    public Integer getImperialStanding() { return imperialStanding; }
    public void setImperialStanding(Integer imperialStanding) { this.imperialStanding = imperialStanding; }

    public Integer getLoginPoints() { return loginPoints; }
    public void setLoginPoints(Integer loginPoints) { this.loginPoints = loginPoints; }

    public Integer getNyzulTokens() { return nyzulTokens; }
    public void setNyzulTokens(Integer nyzulTokens) { this.nyzulTokens = nyzulTokens; }

    public Integer getSparksOfEminence() { return sparksOfEminence; }
    public void setSparksOfEminence(Integer sparksOfEminence) { this.sparksOfEminence = sparksOfEminence; }

    public Integer getTherionIchor() { return therionIchor; }
    public void setTherionIchor(Integer therionIchor) { this.therionIchor = therionIchor; }

    public Integer getUnityAccolades() { return unityAccolades; }
    public void setUnityAccolades(Integer unityAccolades) { this.unityAccolades = unityAccolades; }

    public Integer getVoidstones() { return voidstones; }
    public void setVoidstones(Integer voidstones) { this.voidstones = voidstones; }
}
