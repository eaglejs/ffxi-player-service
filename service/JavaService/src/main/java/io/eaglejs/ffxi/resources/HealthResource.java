package io.eaglejs.ffxi.resources;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Health check endpoints for monitoring service status")
public class HealthResource {

    private final HealthCheckRegistry healthChecks;

    public HealthResource(HealthCheckRegistry healthChecks) {
        this.healthChecks = healthChecks;
    }

    @GET
    @Operation(
        summary = "Service Health Check",
        description = "Returns the health status of the service and its dependencies including database connectivity.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Service is healthy"),
            @ApiResponse(responseCode = "503", description = "Service is unhealthy")
        }
    )
    public Response healthCheck() {
        SortedMap<String, HealthCheck.Result> results = healthChecks.runHealthChecks();
        boolean allHealthy = results.values().stream().allMatch(HealthCheck.Result::isHealthy);

        Map<String, Object> response = new HashMap<>();
        response.put("status", allHealthy ? "ok" : "unhealthy");

        Map<String, Object> checks = new HashMap<>();
        for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
            Map<String, Object> checkDetail = new HashMap<>();
            checkDetail.put("healthy", entry.getValue().isHealthy());
            checkDetail.put("message", entry.getValue().getMessage());
            checks.put(entry.getKey(), checkDetail);
        }
        response.put("checks", checks);

        int statusCode = allHealthy ? 200 : 503;
        return Response.status(statusCode).entity(response).build();
    }
}
