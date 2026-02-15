package io.eaglejs.ffxi.resources;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.Assert.*;

public class HealthResourceTest {

    private HealthCheckRegistry registry;
    private HealthResource resource;

    @Before
    public void setUp() {
        registry = new HealthCheckRegistry();
        resource = new HealthResource(registry);
    }

    @Test
    public void testHealthyResponse() {
        registry.register("test", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.healthy("All good");
            }
        });

        Response response = resource.healthCheck();
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("ok", entity.get("status"));
    }

    @Test
    public void testUnhealthyResponse() {
        registry.register("test", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.unhealthy("Something is wrong");
            }
        });

        Response response = resource.healthCheck();
        assertEquals(503, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("unhealthy", entity.get("status"));
    }

    @Test
    public void testMixedHealthChecks() {
        registry.register("healthy-check", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.healthy("OK");
            }
        });
        registry.register("unhealthy-check", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.unhealthy("Failed");
            }
        });

        Response response = resource.healthCheck();
        assertEquals(503, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("unhealthy", entity.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> checks = (Map<String, Object>) entity.get("checks");
        assertNotNull(checks);
        assertEquals(2, checks.size());
    }

    @Test
    public void testHealthResponseContainsCheckDetails() {
        registry.register("db", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.healthy("Connected");
            }
        });

        Response response = resource.healthCheck();

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        @SuppressWarnings("unchecked")
        Map<String, Object> checks = (Map<String, Object>) entity.get("checks");
        @SuppressWarnings("unchecked")
        Map<String, Object> dbCheck = (Map<String, Object>) checks.get("db");

        assertTrue((Boolean) dbCheck.get("healthy"));
        assertEquals("Connected", dbCheck.get("message"));
    }
}
