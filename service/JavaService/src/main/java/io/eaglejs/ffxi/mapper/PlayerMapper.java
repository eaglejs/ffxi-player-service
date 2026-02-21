package io.eaglejs.ffxi.mapper;

import io.eaglejs.ffxi.models.Player;
import org.bson.Document;

/**
 * Utility class for mapping MongoDB Documents to domain objects.
 */
public class PlayerMapper {

      /**
      * Convert a Player object to a MongoDB Document for storage.
      */
    public static Document playerToDocument(Player player) {
        if (player == null) {
            return null;
        }

        Document document = new Document();
        document.append("playerId", player.getPlayerId());
        document.append("playerName", player.getPlayerName());
        document.append("mainJob", player.getMainJob());
        document.append("mainJobLevel", player.getMainJobLevel());
        document.append("subJob", player.getSubJob());
        document.append("subJobLevel", player.getSubJobLevel());
        document.append("masterLevel", player.getMasterLevel());
        document.append("zone", player.getZone());
        document.append("title", player.getTitle());
        document.append("nationRank", player.getNationRank());
        document.append("status", player.getStatus());
        document.append("lastOnline", player.getLastOnline());
        document.append("gil", player.getGil());
        document.append("hpp", player.getHpp());
        document.append("mpp", player.getMpp());
        document.append("tp", player.getTp());
        document.append("attack", player.getAttack());
        document.append("defense", player.getDefense());
        document.append("currentExemplar", player.getCurrentExemplar());
        document.append("requiredExemplar", player.getRequiredExemplar());

        // For nested objects, we can convert them to sub-documents
        if (player.getStats() != null) {
            Document statsDoc = new Document();
            statsDoc.append("baseSTR", player.getStats().getBaseSTR());
            statsDoc.append("baseDEX", player.getStats().getBaseDEX());
            statsDoc.append("baseVIT", player.getStats().getBaseVIT());
            statsDoc.append("baseAGI", player.getStats().getBaseAGI());
            statsDoc.append("baseINT", player.getStats().getBaseINT());
            statsDoc.append("baseMND", player.getStats().getBaseMND());
            statsDoc.append("baseCHR", player.getStats().getBaseCHR());
            statsDoc.append("addedSTR", player.getStats().getAddedSTR());
            statsDoc.append("addedDEX", player.getStats().getAddedDEX());
            statsDoc.append("addedVIT", player.getStats().getAddedVIT());
            statsDoc.append("addedAGI", player.getStats().getAddedAGI());
            statsDoc.append("addedINT", player.getStats().getAddedINT());
            statsDoc.append("addedMND", player.getStats().getAddedMND());
            statsDoc.append("addedCHR", player.getStats().getAddedCHR());
            statsDoc.append("fireResistance", player.getStats().getFireResistance());
            statsDoc.append("iceResistance", player.getStats().getIceResistance());
            statsDoc.append("windResistance", player.getStats().getWindResistance());
            statsDoc.append("earthResistance", player.getStats().getEarthResistance());
            statsDoc.append("lightningResistance", player.getStats().getLightningResistance());
            statsDoc.append("waterResistance", player.getStats().getWaterResistance());
            statsDoc.append("lightResistance", player.getStats().getLightResistance());
            statsDoc.append("darkResistance", player.getStats().getDarkResistance());
            document.append("stats", statsDoc);
        }
        if (player.getMerits() != null) {
            Document meritsDoc = new Document();
            meritsDoc.append("total", player.getMerits().getTotal());
            meritsDoc.append("max", player.getMerits().getMax());
            document.append("merits", meritsDoc);
        }
        if (player.getCapacityPoints() != null) {
            Document capacityDoc = new Document();
            capacityDoc.append("total", player.getCapacityPoints().getTotal());
            document.append("capacityPoints", capacityDoc);
        }
        if (player.getCurrency1() != null) {
            document.append("currency1", player.getCurrency1());
        }
        if (player.getCurrency2() != null) {
            document.append("currency2", player.getCurrency2());
        }
        if (player.getBuffs() != null) {
            document.append("buffs", player.getBuffs());
        }
        if (player.getAbilities() != null) {
            document.append("abilities", player.getAbilities());
        }
        if (player.getExpHistory() != null) {
            Document expHistoryDoc = new Document();
            // Note: For simplicity, keeping as generic lists
            // You could add more detailed mapping here if needed
            document.append("expHistory", expHistoryDoc);
        }
        return document;
    }

    /**
     * Convert a MongoDB Document to a Player object.
     * Uses Jackson's ObjectMapper for automatic mapping.
     */
    public static Player documentToPlayer(Document document) {
        if (document == null) {
            return null;
        }

        Player player = new Player();
        
        // Map primitive fields
        player.setPlayerId(document.getInteger("playerId"));
        player.setPlayerName(document.getString("playerName"));
        player.setMainJob(document.getString("mainJob"));
        player.setMainJobLevel(document.getInteger("mainJobLevel"));
        player.setSubJob(document.getString("subJob"));
        player.setSubJobLevel(document.getInteger("subJobLevel"));
        player.setMasterLevel(document.getInteger("masterLevel"));
        player.setZone(document.getString("zone"));
        player.setTitle(document.getString("title"));
        player.setNationRank(document.getInteger("nationRank"));
        player.setStatus(document.getInteger("status"));
        player.setLastOnline(document.getLong("lastOnline"));
        player.setGil(document.getInteger("gil"));
        player.setHpp(document.getInteger("hpp"));
        player.setMpp(document.getInteger("mpp"));
        player.setTp(document.getInteger("tp"));
        player.setAttack(document.getInteger("attack"));
        player.setDefense(document.getInteger("defense"));
        player.setCurrentExemplar(document.getInteger("currentExemplar"));
        player.setRequiredExemplar(document.getInteger("requiredExemplar"));

        // Map nested objects
        Document statsDoc = document.get("stats", Document.class);
        if (statsDoc != null) {
            player.setStats(mapStats(statsDoc));
        }

        Document meritsDoc = document.get("merits", Document.class);
        if (meritsDoc != null) {
            Player.Merits merits = new Player.Merits();
            merits.setTotal(meritsDoc.getInteger("total"));
            merits.setMax(meritsDoc.getInteger("max"));
            player.setMerits(merits);
        }

        Document capacityDoc = document.get("capacityPoints", Document.class);
        if (capacityDoc != null) {
            Player.CapacityPoints cp = new Player.CapacityPoints();
            cp.setTotal(capacityDoc.getInteger("total"));
            player.setCapacityPoints(cp);
        }

        // Map complex fields (keep as generic maps/lists for flexibility)
        Document currency1Doc = document.get("currency1", Document.class);
        if (currency1Doc != null) {
            player.setCurrency1(currency1Doc);
        }
        Document currency2Doc = document.get("currency2", Document.class);
        if (currency2Doc != null) {
            player.setCurrency2(currency2Doc);
        }
        Document buffsDoc = document.get("buffs", Document.class);
        if (buffsDoc != null) {
            player.setBuffs(buffsDoc);
        }
        player.setAbilities(document.getList("abilities", Object.class));
        
        Document expHistoryDoc = document.get("expHistory", Document.class);
        if (expHistoryDoc != null) {
            player.setExpHistory(mapExpHistory(expHistoryDoc));
        }

        return player;
    }

    private static Player.PlayerStats mapStats(Document statsDoc) {
        Player.PlayerStats stats = new Player.PlayerStats();
        stats.setBaseSTR(statsDoc.getInteger("baseSTR"));
        stats.setBaseDEX(statsDoc.getInteger("baseDEX"));
        stats.setBaseVIT(statsDoc.getInteger("baseVIT"));
        stats.setBaseAGI(statsDoc.getInteger("baseAGI"));
        stats.setBaseINT(statsDoc.getInteger("baseINT"));
        stats.setBaseMND(statsDoc.getInteger("baseMND"));
        stats.setBaseCHR(statsDoc.getInteger("baseCHR"));
        stats.setAddedSTR(statsDoc.getInteger("addedSTR"));
        stats.setAddedDEX(statsDoc.getInteger("addedDEX"));
        stats.setAddedVIT(statsDoc.getInteger("addedVIT"));
        stats.setAddedAGI(statsDoc.getInteger("addedAGI"));
        stats.setAddedINT(statsDoc.getInteger("addedINT"));
        stats.setAddedMND(statsDoc.getInteger("addedMND"));
        stats.setAddedCHR(statsDoc.getInteger("addedCHR"));
        stats.setFireResistance(statsDoc.getInteger("fireResistance"));
        stats.setIceResistance(statsDoc.getInteger("iceResistance"));
        stats.setWindResistance(statsDoc.getInteger("windResistance"));
        stats.setEarthResistance(statsDoc.getInteger("earthResistance"));
        stats.setLightningResistance(statsDoc.getInteger("lightningResistance"));
        stats.setWaterResistance(statsDoc.getInteger("waterResistance"));
        stats.setLightResistance(statsDoc.getInteger("lightResistance"));
        stats.setDarkResistance(statsDoc.getInteger("darkResistance"));
        return stats;
    }

    private static Player.ExpHistory mapExpHistory(Document expHistoryDoc) {
        Player.ExpHistory expHistory = new Player.ExpHistory();
        // Note: For simplicity, keeping as generic lists
        // You could add more detailed mapping here if needed
        return expHistory;
    }
}
