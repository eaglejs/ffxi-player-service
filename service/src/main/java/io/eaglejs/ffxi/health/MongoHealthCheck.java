package io.eaglejs.ffxi.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

public class MongoHealthCheck extends HealthCheck {

    private final String mongoUri;

    public MongoHealthCheck(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    @Override
    protected Result check() {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            Document result = client.getDatabase("admin").runCommand(new Document("ping", 1));
            if (result.getDouble("ok") == 1.0) {
                return Result.healthy("MongoDB connection is healthy");
            }
            return Result.unhealthy("MongoDB ping returned unexpected result");
        } catch (Exception e) {
            return Result.unhealthy("MongoDB connection failed: " + e.getMessage());
        }
    }
}
