package io.eaglejs.ffxi.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import io.eaglejs.ffxi.dao.InventoryDao.SaveResult;
import io.eaglejs.ffxi.dao.InventoryDao.SearchCriteria;
import io.eaglejs.ffxi.models.InventoryItem;
import io.eaglejs.ffxi.models.InventorySnapshotRequest;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class InventoryDaoTest {

    private MongoCollection<Document> collection;
    private FindIterable<Document> findIterable;
    private InventoryDao dao;

    @Before
    public void setUp() {
        collection = mock(MongoCollection.class);
        findIterable = mock(FindIterable.class);
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        dao = new InventoryDao(collection);
    }

    private static InventorySnapshotRequest request(String hash, long timestamp) {
        InventoryItem item = new InventoryItem();
        item.setId(4096);
        item.setCount(3);
        InventorySnapshotRequest request = new InventorySnapshotRequest();
        request.setPlayerId(7);
        request.setPlayerName("Sungjinwoo");
        request.setContentHash(hash);
        request.setTimestamp(timestamp);
        request.setGil(500L);
        request.setBags(Map.of("inventory", List.of(item)));
        return request;
    }

    @Test
    public void insertsWhenNoExistingSnapshot() {
        when(findIterable.first()).thenReturn(null);

        assertEquals(SaveResult.SAVED, dao.save(request("h1", 100)));

        ArgumentCaptor<UpdateOptions> options = ArgumentCaptor.forClass(UpdateOptions.class);
        verify(collection).updateOne(any(Bson.class), any(Bson.class), options.capture());
        assertTrue(options.getValue().isUpsert());
    }

    @Test
    public void replacesWhenHashDiffers() {
        when(findIterable.first()).thenReturn(new Document("contentHash", "old").append("clientTimestamp", 100L));

        assertEquals(SaveResult.SAVED, dao.save(request("new", 200)));
        verify(collection).updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class));
    }

    @Test
    public void sameHashIsNoOp() {
        when(findIterable.first()).thenReturn(new Document("contentHash", "h1").append("clientTimestamp", 100L));

        assertEquals(SaveResult.UNCHANGED, dao.save(request("h1", 200)));
        verify(collection, never()).updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class));
    }

    @Test
    public void olderTimestampIsIgnoredAsStale() {
        when(findIterable.first()).thenReturn(new Document("contentHash", "old").append("clientTimestamp", 300L));

        assertEquals(SaveResult.STALE, dao.save(request("new", 200)));
        verify(collection, never()).updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class));
    }

    @Test
    public void listCharactersProjectsSummariesInNameOrder() {
        FindIterable<Document> projected = mock(FindIterable.class);
        FindIterable<Document> sorted = mock(FindIterable.class);
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.projection(any(Bson.class))).thenReturn(projected);
        when(projected.sort(any(Bson.class))).thenReturn(sorted);
        com.mongodb.client.MongoCursor<Document> cursor = mock(com.mongodb.client.MongoCursor.class);
        when(sorted.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(new Document("playerId", 7).append("playerName", "Alpha").append("gil", 5L)
                .append("updatedAt", new java.util.Date(0)));

        List<Map<String, Object>> characters = dao.listCharacters();

        assertEquals(1, characters.size());
        assertEquals(7, characters.get(0).get("playerId"));
        assertEquals("Alpha", characters.get(0).get("playerName"));
        assertEquals("1970-01-01T00:00:00Z", characters.get(0).get("updatedAt"));
    }

    @Test
    public void searchPipelineFiltersPlayerBagAndItems() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.itemIds = List.of(4096, 4097);
        criteria.playerNameLower = "sungjinwoo";
        criteria.bags = List.of("wardrobe");
        criteria.limit = 10;
        criteria.offset = 20;

        List<Bson> pipeline = InventoryDao.buildSearchPipeline(criteria);
        String json = pipeline.toString();

        assertTrue(json.contains("playerNameLower"));
        assertTrue(json.contains("$objectToArray"));
        assertTrue(json.contains("bagArr.k"));
        assertTrue(json.contains("4096"));
        assertTrue(!json.contains("keyItems"));
        Document facet = (Document) ((Document) pipeline.get(pipeline.size() - 1)).get("$facet");
        List<Document> results = (List<Document>) facet.get("results");
        assertEquals(20, results.get(0).get("$skip"));
        assertEquals(10, results.get(1).get("$limit"));
        // player $match comes first so the playerNameLower index is used
        assertTrue(((Document) pipeline.get(0)).containsKey("$match"));
    }

    @Test
    public void searchPipelineIncludesKeyItemsOnlyWhenRequested() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.itemIds = List.of(1);
        criteria.includeKeyItems = true;

        String json = InventoryDao.buildSearchPipeline(criteria).toString();
        assertTrue(json.contains("keyItems"));
        assertTrue(json.contains("$concatArrays"));
        // no player filter: first stage is the projection
        assertTrue(((Document) InventoryDao.buildSearchPipeline(criteria).get(0)).containsKey("$project"));
    }
}
