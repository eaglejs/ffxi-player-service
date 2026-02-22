package io.eaglejs.ffxi.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Service for managing MongoDB connections and providing access to collections.
 */
public class MongoDBService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDBService(String mongoUri) {
        this.mongoClient = MongoClients.create(mongoUri);
        this.database = mongoClient.getDatabase("ffxi");
    }

    /**
     * Get the players collection from the ffxi database.
     */
    public MongoCollection<Document> getPlayersCollection() {
        return database.getCollection("players");
    }

    /**
     * Get the chats collection from the ffxi database.
     */
    public MongoCollection<Document> getChatsCollection() {
        return database.getCollection("chats");
    }
    
    /**
     * Close the MongoDB client connection.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}

