package io.eaglejs.ffxi.resources;

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


@Path("/players")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Players", description = "Endpoints for retrieving player information and stats")
public class PlayerResource {
  
    // get_players   @GET /players/get_players
    @GET
    @Path("/get_players")
    @Operation(
        summary = "Get Player List",
        description = "Returns a list of all players with their basic information.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of players retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response getPlayers() {
        // get players from mongodb and return as json
        Map<String, Object> response = new HashMap<>();
        response.put("players", new String[]{}); // Placeholder - replace with actual player data
        return Response.ok(response).build();
    }
}
