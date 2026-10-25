package io.eaglejs.ffxi.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class InventorySnapshotRequestTest {

    private static InventoryItem item(Integer id, Integer count) {
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setCount(count);
        return item;
    }

    private static InventorySnapshotRequest validRequest() {
        InventorySnapshotRequest request = new InventorySnapshotRequest();
        request.setPlayerId(1);
        request.setPlayerName("Sungjinwoo");
        request.setContentHash("abc");
        request.setGil(100L);
        Map<String, List<InventoryItem>> bags = new LinkedHashMap<>();
        bags.put("inventory", List.of(item(4096, 12)));
        bags.put("slip 01", List.of(item(1, 1)));
        request.setBags(bags);
        request.setKeyItems(List.of(1, 2));
        return request;
    }

    @Test
    public void validRequestHasNoErrors() {
        assertTrue(validRequest().validate().isEmpty());
    }

    @Test
    public void missingRequiredFieldsAreAllReported() {
        InventorySnapshotRequest request = new InventorySnapshotRequest();
        List<String> errors = request.validate();
        assertEquals(3, errors.size());
        assertTrue(errors.contains("playerId is required"));
        assertTrue(errors.contains("playerName is required"));
        assertTrue(errors.contains("contentHash is required"));
    }

    @Test
    public void negativeGilIsRejected() {
        InventorySnapshotRequest request = validRequest();
        request.setGil(-1L);
        assertTrue(request.validate().contains("gil must be >= 0"));
    }

    @Test
    public void zeroCountIsRejected() {
        InventorySnapshotRequest request = validRequest();
        request.getBags().put("safe", List.of(item(5, 0)));
        assertTrue(request.validate().contains("bags.safe[0].count must be >= 1"));
    }

    @Test
    public void nullItemAndMissingIdAreRejected() {
        InventorySnapshotRequest request = validRequest();
        List<InventoryItem> items = new ArrayList<>();
        items.add(null);
        items.add(item(null, 1));
        request.getBags().put("satchel", items);
        List<String> errors = request.validate();
        assertTrue(errors.contains("bags.satchel[0] must not be null"));
        assertTrue(errors.contains("bags.satchel[1].id must be a non-negative item id"));
    }

    @Test
    public void unsafeBagNamesAreRejected() {
        InventorySnapshotRequest request = validRequest();
        request.getBags().put("a.b", List.of());
        request.getBags().put("$bad", List.of());
        assertEquals(2, request.validate().size());
    }

    @Test
    public void newBagNamesAreAccepted() {
        InventorySnapshotRequest request = validRequest();
        request.getBags().put("wardrobe8", List.of(item(1, 1)));
        request.getBags().put("slip 33", List.of(item(2, 1)));
        assertTrue(request.validate().isEmpty());
    }
}
