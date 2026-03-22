package io.eaglejs.ffxi.health;

import com.codahale.metrics.health.HealthCheck;
import org.junit.Test;

import static org.junit.Assert.*;

public class MongoHealthCheckTest {

    @Test
    public void testUnhealthyWhenMongoUnavailable() {
        MongoHealthCheck healthCheck = new MongoHealthCheck("mongodb://localhost:99999");
        HealthCheck.Result result = healthCheck.check();
        assertFalse("Health check should be unhealthy when MongoDB is unavailable", result.isHealthy());
        assertNotNull("Message should not be null", result.getMessage());
        assertTrue("Message should indicate connection failure",
                result.getMessage().contains("MongoDB connection failed"));
    }

    @Test
    public void testUnhealthyWithInvalidUri() {
        MongoHealthCheck healthCheck = new MongoHealthCheck("invalid-uri");
        HealthCheck.Result result = healthCheck.check();
        assertFalse("Health check should be unhealthy with invalid URI", result.isHealthy());
    }
}
