package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Player data transfer object representing a player from the database.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {

    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("mainJob")
    private String mainJob;

    @JsonProperty("mainJobLevel")
    private Integer mainJobLevel;

    @JsonProperty("subJob")
    private String subJob;

    @JsonProperty("subJobLevel")
    private Integer subJobLevel;

    @JsonProperty("masterLevel")
    private Integer masterLevel;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("title")
    private String title;

    @JsonProperty("nationRank")
    private Integer nationRank;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("lastOnline")
    private Long lastOnline;

    @JsonProperty("gil")
    private Integer gil;

    @JsonProperty("hpp")
    private Integer hpp;

    @JsonProperty("mpp")
    private Integer mpp;

    @JsonProperty("tp")
    private Integer tp;

    @JsonProperty("attack")
    private Integer attack;

    @JsonProperty("defense")
    private Integer defense;

    @JsonProperty("stats")
    private PlayerStats stats;

    @JsonProperty("merits")
    private Merits merits;

    @JsonProperty("capacityPoints")
    private CapacityPoints capacityPoints;

    @JsonProperty("currentExemplar")
    private Integer currentExemplar;

    @JsonProperty("requiredExemplar")
    private Integer requiredExemplar;

    @JsonProperty("currency1")
    private Map<String, Object> currency1;

    @JsonProperty("currency2")
    private Map<String, Object> currency2;

    @JsonProperty("buffs")
    private Map<String, Object> buffs;

    @JsonProperty("abilities")
    private List<Object> abilities;

    @JsonProperty("expHistory")
    private ExpHistory expHistory;

    // Constructors
    public Player() {
    }

    // Getters and Setters
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

    public Integer getMainJobLevel() {
        return mainJobLevel;
    }

    public void setMainJobLevel(Integer mainJobLevel) {
        this.mainJobLevel = mainJobLevel;
    }

    public String getSubJob() {
        return subJob;
    }

    public void setSubJob(String subJob) {
        this.subJob = subJob;
    }

    public Integer getSubJobLevel() {
        return subJobLevel;
    }

    public void setSubJobLevel(Integer subJobLevel) {
        this.subJobLevel = subJobLevel;
    }

    public Integer getMasterLevel() {
        return masterLevel;
    }

    public void setMasterLevel(Integer masterLevel) {
        this.masterLevel = masterLevel;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNationRank() {
        return nationRank;
    }

    public void setNationRank(Integer nationRank) {
        this.nationRank = nationRank;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public Integer getGil() {
        return gil;
    }

    public void setGil(Integer gil) {
        this.gil = gil;
    }

    public Integer getHpp() {
        return hpp;
    }

    public void setHpp(Integer hpp) {
        this.hpp = hpp;
    }

    public Integer getMpp() {
        return mpp;
    }

    public void setMpp(Integer mpp) {
        this.mpp = mpp;
    }

    public Integer getTp() {
        return tp;
    }

    public void setTp(Integer tp) {
        this.tp = tp;
    }

    public Integer getAttack() {
        return attack;
    }

    public void setAttack(Integer attack) {
        this.attack = attack;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public Merits getMerits() {
        return merits;
    }

    public void setMerits(Merits merits) {
        this.merits = merits;
    }

    public CapacityPoints getCapacityPoints() {
        return capacityPoints;
    }

    public void setCapacityPoints(CapacityPoints capacityPoints) {
        this.capacityPoints = capacityPoints;
    }

    public Integer getCurrentExemplar() {
        return currentExemplar;
    }

    public void setCurrentExemplar(Integer currentExemplar) {
        this.currentExemplar = currentExemplar;
    }

    public Integer getRequiredExemplar() {
        return requiredExemplar;
    }

    public void setRequiredExemplar(Integer requiredExemplar) {
        this.requiredExemplar = requiredExemplar;
    }

    public Map<String, Object> getCurrency1() {
        return currency1;
    }

    public void setCurrency1(Map<String, Object> currency1) {
        this.currency1 = currency1;
    }

    public Map<String, Object> getCurrency2() {
        return currency2;
    }

    public void setCurrency2(Map<String, Object> currency2) {
        this.currency2 = currency2;
    }

    public Map<String, Object> getBuffs() {
        return buffs;
    }

    public void setBuffs(Map<String, Object> buffs) {
        this.buffs = buffs;
    }

    public List<Object> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<Object> abilities) {
        this.abilities = abilities;
    }

    public ExpHistory getExpHistory() {
        return expHistory;
    }

    public void setExpHistory(ExpHistory expHistory) {
        this.expHistory = expHistory;
    }

    // Nested classes for complex types
    public static class PlayerStats {
        @JsonProperty("baseSTR")
        private Integer baseSTR;
        @JsonProperty("baseDEX")
        private Integer baseDEX;
        @JsonProperty("baseVIT")
        private Integer baseVIT;
        @JsonProperty("baseAGI")
        private Integer baseAGI;
        @JsonProperty("baseINT")
        private Integer baseINT;
        @JsonProperty("baseMND")
        private Integer baseMND;
        @JsonProperty("baseCHR")
        private Integer baseCHR;
        @JsonProperty("addedSTR")
        private Integer addedSTR;
        @JsonProperty("addedDEX")
        private Integer addedDEX;
        @JsonProperty("addedVIT")
        private Integer addedVIT;
        @JsonProperty("addedAGI")
        private Integer addedAGI;
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

        // Getters and setters omitted for brevity - IDE can generate these
        public Integer getBaseSTR() { return baseSTR; }
        public void setBaseSTR(Integer baseSTR) { this.baseSTR = baseSTR; }
        public Integer getBaseDEX() { return baseDEX; }
        public void setBaseDEX(Integer baseDEX) { this.baseDEX = baseDEX; }
        public Integer getBaseVIT() { return baseVIT; }
        public void setBaseVIT(Integer baseVIT) { this.baseVIT = baseVIT; }
        public Integer getBaseAGI() { return baseAGI; }
        public void setBaseAGI(Integer baseAGI) { this.baseAGI = baseAGI; }
        public Integer getBaseINT() { return baseINT; }
        public void setBaseINT(Integer baseINT) { this.baseINT = baseINT; }
        public Integer getBaseMND() { return baseMND; }
        public void setBaseMND(Integer baseMND) { this.baseMND = baseMND; }
        public Integer getBaseCHR() { return baseCHR; }
        public void setBaseCHR(Integer baseCHR) { this.baseCHR = baseCHR; }
        public Integer getAddedSTR() { return addedSTR; }
        public void setAddedSTR(Integer addedSTR) { this.addedSTR = addedSTR; }
        public Integer getAddedDEX() { return addedDEX; }
        public void setAddedDEX(Integer addedDEX) { this.addedDEX = addedDEX; }
        public Integer getAddedVIT() { return addedVIT; }
        public void setAddedVIT(Integer addedVIT) { this.addedVIT = addedVIT; }
        public Integer getAddedAGI() { return addedAGI; }
        public void setAddedAGI(Integer addedAGI) { this.addedAGI = addedAGI; }
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

    public static class Merits {
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("max")
        private Integer max;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
        public Integer getMax() { return max; }
        public void setMax(Integer max) { this.max = max; }
    }

    public static class CapacityPoints {
        @JsonProperty("total")
        private Integer total;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
    }

    public static class ExpHistory {
        @JsonProperty("experience")
        private List<ExpEntry> experience;
        @JsonProperty("capacity")
        private List<ExpEntry> capacity;
        @JsonProperty("exemplar")
        private List<ExpEntry> exemplar;

        public List<ExpEntry> getExperience() { return experience; }
        public void setExperience(List<ExpEntry> experience) { this.experience = experience; }
        public List<ExpEntry> getCapacity() { return capacity; }
        public void setCapacity(List<ExpEntry> capacity) { this.capacity = capacity; }
        public List<ExpEntry> getExemplar() { return exemplar; }
        public void setExemplar(List<ExpEntry> exemplar) { this.exemplar = exemplar; }
    }

    public static class ExpEntry {
        @JsonProperty("points")
        private Integer points;
        @JsonProperty("chain")
        private Integer chain;
        @JsonProperty("timestamp")
        private String timestamp;

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
        public Integer getChain() { return chain; }
        public void setChain(Integer chain) { this.chain = chain; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
