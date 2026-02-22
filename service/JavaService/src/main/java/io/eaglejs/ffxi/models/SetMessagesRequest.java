package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.bson.Document;

public class SetMessagesRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("messagesPackage")
    private List<Document> messagesPackage;

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

    public List<Document> getMessagesPackage() {
        return messagesPackage;
    }

    public void setMessagesPackage(List<Document> messagesPackage) {
        this.messagesPackage = messagesPackage;
    }
}
