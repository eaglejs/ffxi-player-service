package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Full per-character inventory snapshot posted by the Python PlayerService.
 * Bag names are opaque map keys (the game adds new bags/slips over time).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventorySnapshotRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("contentHash")
    private String contentHash;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("gil")
    private Long gil;

    @JsonProperty("keyItems")
    private List<Integer> keyItems;

    @JsonProperty("bags")
    private Map<String, List<InventoryItem>> bags;

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

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getGil() {
        return gil;
    }

    public void setGil(Long gil) {
        this.gil = gil;
    }

    public List<Integer> getKeyItems() {
        return keyItems;
    }

    public void setKeyItems(List<Integer> keyItems) {
        this.keyItems = keyItems;
    }

    public Map<String, List<InventoryItem>> getBags() {
        return bags;
    }

    public void setBags(Map<String, List<InventoryItem>> bags) {
        this.bags = bags;
    }

    /**
     * Returns a description of every invalid field; empty when the request is valid.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (playerId == null) {
            errors.add("playerId is required");
        }
        if (playerName == null || playerName.isBlank()) {
            errors.add("playerName is required");
        }
        if (contentHash == null || contentHash.isBlank()) {
            errors.add("contentHash is required");
        }
        if (gil != null && gil < 0) {
            errors.add("gil must be >= 0");
        }
        if (keyItems != null) {
            for (int i = 0; i < keyItems.size(); i++) {
                if (keyItems.get(i) == null || keyItems.get(i) < 0) {
                    errors.add("keyItems[" + i + "] must be a non-negative item id");
                }
            }
        }
        if (bags != null) {
            for (Map.Entry<String, List<InventoryItem>> bag : bags.entrySet()) {
                String bagName = bag.getKey();
                if (bagName == null || bagName.isEmpty() || bagName.contains(".") || bagName.startsWith("$")) {
                    errors.add("bags has an invalid bag name: '" + bagName + "'");
                    continue;
                }
                List<InventoryItem> items = bag.getValue();
                if (items == null) {
                    errors.add("bags." + bagName + " must be an array");
                    continue;
                }
                for (int i = 0; i < items.size(); i++) {
                    InventoryItem item = items.get(i);
                    String prefix = "bags." + bagName + "[" + i + "]";
                    if (item == null) {
                        errors.add(prefix + " must not be null");
                        continue;
                    }
                    if (item.getId() == null || item.getId() < 0) {
                        errors.add(prefix + ".id must be a non-negative item id");
                    }
                    if (item.getCount() == null || item.getCount() < 1) {
                        errors.add(prefix + ".count must be >= 1");
                    }
                }
            }
        }
        return errors;
    }
}
