package io.eaglejs.ffxi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SetMessagesRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("messages")
    private List<Object> messages;

    @JsonProperty("messageType")
    private String messageType;

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

    public List<Object> getMessages() {
        return messages;
    }

    public void setMessages(List<Object> messages) {
        this.messages = messages;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
