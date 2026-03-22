package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats {
    @JsonProperty("baseSTR")
    private Integer baseSTR;

    @JsonProperty("baseAGI")
    private Integer baseAGI;

    @JsonProperty("baseDEX")
    private Integer baseDEX;

    @JsonProperty("baseVIT")
    private Integer baseVIT;

    @JsonProperty("baseINT")
    private Integer baseINT;

    @JsonProperty("baseMND")
    private Integer baseMND;

    @JsonProperty("baseCHR")
    private Integer baseCHR;

    @JsonProperty("addedSTR")
    private Integer addedSTR;

    @JsonProperty("addedAGI")
    private Integer addedAGI;

    @JsonProperty("addedDEX")
    private Integer addedDEX;

    @JsonProperty("addedVIT")
    private Integer addedVIT;

    @JsonProperty("addedINT")
    private Integer addedINT;

    @JsonProperty("addedMND")
    private Integer addedMND;

    @JsonProperty("addedCHR")
    private Integer addedCHR;

    @JsonProperty("fireResistance")
    private Integer fireResistance;

    @JsonProperty("iceResistance")
    private Integer iceResistance;

    @JsonProperty("windResistance")
    private Integer windResistance;

    @JsonProperty("earthResistance")
    private Integer earthResistance;

    @JsonProperty("lightningResistance")
    private Integer lightningResistance;

    @JsonProperty("waterResistance")
    private Integer waterResistance;

    @JsonProperty("lightResistance")
    private Integer lightResistance;

    @JsonProperty("darkResistance")
    private Integer darkResistance;

    // Getters and Setters
    public Integer getBaseSTR() { return baseSTR; }
    public void setBaseSTR(Integer baseSTR) { this.baseSTR = baseSTR; }

    public Integer getBaseAGI() { return baseAGI; }
    public void setBaseAGI(Integer baseAGI) { this.baseAGI = baseAGI; }

    public Integer getBaseDEX() { return baseDEX; }
    public void setBaseDEX(Integer baseDEX) { this.baseDEX = baseDEX; }

    public Integer getBaseVIT() { return baseVIT; }
    public void setBaseVIT(Integer baseVIT) { this.baseVIT = baseVIT; }

    public Integer getBaseINT() { return baseINT; }
    public void setBaseINT(Integer baseINT) { this.baseINT = baseINT; }

    public Integer getBaseMND() { return baseMND; }
    public void setBaseMND(Integer baseMND) { this.baseMND = baseMND; }

    public Integer getBaseCHR() { return baseCHR; }
    public void setBaseCHR(Integer baseCHR) { this.baseCHR = baseCHR; }

    public Integer getAddedSTR() { return addedSTR; }
    public void setAddedSTR(Integer addedSTR) { this.addedSTR = addedSTR; }

    public Integer getAddedAGI() { return addedAGI; }
    public void setAddedAGI(Integer addedAGI) { this.addedAGI = addedAGI; }

    public Integer getAddedDEX() { return addedDEX; }
    public void setAddedDEX(Integer addedDEX) { this.addedDEX = addedDEX; }

    public Integer getAddedVIT() { return addedVIT; }
    public void setAddedVIT(Integer addedVIT) { this.addedVIT = addedVIT; }

    public Integer getAddedINT() { return addedINT; }
    public void setAddedINT(Integer addedINT) { this.addedINT = addedINT; }

    public Integer getAddedMND() { return addedMND; }
    public void setAddedMND(Integer addedMND) { this.addedMND = addedMND; }

    public Integer getAddedCHR() { return addedCHR; }
    public void setAddedCHR(Integer addedCHR) { this.addedCHR = addedCHR; }

    public Integer getFireResistance() { return fireResistance; }
    public void setFireResistance(Integer fireResistance) { this.fireResistance = fireResistance; }

    public Integer getIceResistance() { return iceResistance; }
    public void setIceResistance(Integer iceResistance) { this.iceResistance = iceResistance; }

    public Integer getWindResistance() { return windResistance; }
    public void setWindResistance(Integer windResistance) { this.windResistance = windResistance; }

    public Integer getEarthResistance() { return earthResistance; }
    public void setEarthResistance(Integer earthResistance) { this.earthResistance = earthResistance; }

    public Integer getLightningResistance() { return lightningResistance; }
    public void setLightningResistance(Integer lightningResistance) { this.lightningResistance = lightningResistance; }

    public Integer getWaterResistance() { return waterResistance; }
    public void setWaterResistance(Integer waterResistance) { this.waterResistance = waterResistance; }

    public Integer getLightResistance() { return lightResistance; }
    public void setLightResistance(Integer lightResistance) { this.lightResistance = lightResistance; }

    public Integer getDarkResistance() { return darkResistance; }
    public void setDarkResistance(Integer darkResistance) { this.darkResistance = darkResistance; }
}
