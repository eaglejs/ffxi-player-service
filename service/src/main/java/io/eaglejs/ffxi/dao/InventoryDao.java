package io.eaglejs.ffxi.dao;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.setOnInsert;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import io.eaglejs.ffxi.models.InventoryItem;
import io.eaglejs.ffxi.models.InventorySnapshotRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistence for per-character inventory snapshots (collection 'inventories').
 */
public class InventoryDao {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryDao.class);

    public static final String KEY_ITEMS_BAG = "keyItems";

    public enum SaveResult {
        SAVED, UNCHANGED, STALE
    }

    /** Search parameters; at most one of playerId / playerNameLower is set. */
    public static class SearchCriteria {
        public List<Integer> itemIds = new ArrayList<>();
        public Integer playerId;
        public String playerNameLower;
        public List<String> bags = new ArrayList<>();
        public boolean includeKeyItems;
        public int limit = 100;
        public int offset = 0;
    }

    public static class SearchPage {
        public final long totalMatches;
        public final List<Map<String, Object>> results;

        public SearchPage(long totalMatches, List<Map<String, Object>> results) {
            this.totalMatches = totalMatches;
            this.results = results;
        }
    }

    private final MongoCollection<Document> collection;

    public InventoryDao(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    /**
     * Creates the indexes. Failures are logged rather than thrown so an unreachable
     * database at startup does not stop the service from booting.
     */
    public void ensureIndexes() {
        try {
            collection.createIndex(Indexes.ascending("playerId"), new IndexOptions().unique(true));
            collection.createIndex(Indexes.ascending("playerNameLower"));
        } catch (Exception e) {
            LOG.warn("Could not create inventory indexes: {}", e.getMessage());
        }
    }

    /**
     * Replaces the stored snapshot for the player. Skips the write when the content hash
     * is unchanged and ignores snapshots older than the stored one.
     */
    public SaveResult save(InventorySnapshotRequest request) {
        long clientTimestamp = request.getTimestamp() != null
                ? request.getTimestamp()
                : Instant.now().getEpochSecond();

        Document existing = collection.find(eq("playerId", request.getPlayerId())).first();
        if (existing != null) {
            Number storedTimestamp = existing.get("clientTimestamp", Number.class);
            if (storedTimestamp != null && clientTimestamp < storedTimestamp.longValue()) {
                return SaveResult.STALE;
            }
            if (request.getContentHash().equals(existing.getString("contentHash"))) {
                return SaveResult.UNCHANGED;
            }
        }

        Date now = new Date();
        collection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                        set("playerName", request.getPlayerName()),
                        set("playerNameLower", request.getPlayerName().toLowerCase()),
                        set("gil", request.getGil() != null ? request.getGil() : 0L),
                        set("keyItems", request.getKeyItems() != null ? request.getKeyItems() : new ArrayList<Integer>()),
                        set("bags", toBagsDocument(request)),
                        set("contentHash", request.getContentHash()),
                        set("clientTimestamp", clientTimestamp),
                        set("updatedAt", now),
                        setOnInsert("playerId", request.getPlayerId()),
                        setOnInsert("createdAt", now)),
                new UpdateOptions().upsert(true));
        return SaveResult.SAVED;
    }

    public Document findByPlayerId(int playerId) {
        return collection.find(eq("playerId", playerId)).first();
    }

    public boolean playerExists(SearchCriteria criteria) {
        if (criteria.playerId != null) {
            return collection.find(eq("playerId", criteria.playerId)).first() != null;
        }
        if (criteria.playerNameLower != null) {
            return collection.find(eq("playerNameLower", criteria.playerNameLower)).first() != null;
        }
        return true;
    }

    public SearchPage search(SearchCriteria criteria) {
        Document facet = collection.aggregate(buildSearchPipeline(criteria)).first();
        if (facet == null) {
            return new SearchPage(0, new ArrayList<>());
        }

        long total = 0;
        List<Document> meta = facet.getList("meta", Document.class);
        if (meta != null && !meta.isEmpty()) {
            total = meta.get(0).get("total", Number.class).longValue();
        }

        boolean multipleItems = criteria.itemIds.size() > 1;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Document row : facet.getList("results", Document.class)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("playerId", row.get("_id"));
            result.put("playerName", row.get("playerName"));
            result.put("totalCount", row.get("totalCount"));

            List<Document> locations = new ArrayList<>(row.getList("locations", Document.class));
            locations.sort(Comparator
                    .comparing((Document l) -> l.getString("bag"))
                    .thenComparing(l -> l.get("itemId", Number.class).intValue()));
            List<Map<String, Object>> locationList = new ArrayList<>();
            for (Document location : locations) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("bag", location.getString("bag"));
                if (multipleItems) {
                    entry.put("itemId", location.get("itemId"));
                }
                entry.put("count", location.get("count"));
                locationList.add(entry);
            }
            result.put("locations", locationList);

            Object updatedAt = row.get("updatedAt");
            result.put("updatedAt", updatedAt instanceof Date ? ((Date) updatedAt).toInstant().toString() : null);
            results.add(result);
        }
        return new SearchPage(total, results);
    }

    /**
     * Builds the aggregation: match player (indexed) -> flatten bags (and optionally key items)
     * -> keep only the searched item ids -> sum per player/bag/item -> group per player ->
     * sort and paginate with a $facet that also yields the total player count.
     */
    public static List<Bson> buildSearchPipeline(SearchCriteria criteria) {
        List<Bson> pipeline = new ArrayList<>();

        if (criteria.playerId != null) {
            pipeline.add(new Document("$match", new Document("playerId", criteria.playerId)));
        } else if (criteria.playerNameLower != null) {
            pipeline.add(new Document("$match", new Document("playerNameLower", criteria.playerNameLower)));
        }

        Document bagsAsArray = new Document("$objectToArray", new Document("$ifNull", List.of("$bags", new Document())));
        Object bagArray = bagsAsArray;
        if (criteria.includeKeyItems) {
            Document keyItemsBag = new Document("k", KEY_ITEMS_BAG).append("v",
                    new Document("$map", new Document("input", new Document("$ifNull", List.of("$keyItems", List.of())))
                            .append("as", "k")
                            .append("in", new Document("id", "$$k").append("count", 1))));
            bagArray = new Document("$concatArrays", List.of(bagsAsArray, List.of(keyItemsBag)));
        }
        pipeline.add(new Document("$project", new Document("playerId", 1)
                .append("playerName", 1)
                .append("updatedAt", 1)
                .append("bagArr", bagArray)));
        pipeline.add(new Document("$unwind", "$bagArr"));

        if (!criteria.bags.isEmpty()) {
            pipeline.add(new Document("$match", new Document("bagArr.k", new Document("$in", criteria.bags))));
        }

        pipeline.add(new Document("$project", new Document("playerId", 1)
                .append("playerName", 1)
                .append("updatedAt", 1)
                .append("bag", "$bagArr.k")
                .append("hits", new Document("$filter", new Document("input", "$bagArr.v")
                        .append("as", "it")
                        .append("cond", new Document("$in", List.of("$$it.id", criteria.itemIds)))))));
        pipeline.add(new Document("$unwind", "$hits"));

        pipeline.add(new Document("$group", new Document("_id",
                new Document("playerId", "$playerId").append("bag", "$bag").append("itemId", "$hits.id"))
                .append("playerName", new Document("$first", "$playerName"))
                .append("updatedAt", new Document("$first", "$updatedAt"))
                .append("count", new Document("$sum", "$hits.count"))));
        pipeline.add(new Document("$group", new Document("_id", "$_id.playerId")
                .append("playerName", new Document("$first", "$playerName"))
                .append("updatedAt", new Document("$first", "$updatedAt"))
                .append("totalCount", new Document("$sum", "$count"))
                .append("locations", new Document("$push", new Document("bag", "$_id.bag")
                        .append("itemId", "$_id.itemId")
                        .append("count", "$count")))));

        pipeline.add(new Document("$sort", new Document("totalCount", -1).append("playerName", 1)));
        pipeline.add(new Document("$facet", new Document("meta", List.of(new Document("$count", "total")))
                .append("results", List.of(
                        new Document("$skip", criteria.offset),
                        new Document("$limit", criteria.limit)))));
        return pipeline;
    }

    private static Document toBagsDocument(InventorySnapshotRequest request) {
        Document bags = new Document();
        if (request.getBags() != null) {
            for (Map.Entry<String, List<InventoryItem>> bag : request.getBags().entrySet()) {
                List<Document> items = new ArrayList<>();
                for (InventoryItem item : bag.getValue()) {
                    items.add(new Document("id", item.getId()).append("count", item.getCount()));
                }
                bags.put(bag.getKey(), items);
            }
        }
        return bags;
    }
}
