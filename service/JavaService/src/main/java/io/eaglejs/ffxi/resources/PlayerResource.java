package io.eaglejs.ffxi.resources;

import com.mongodb.client.MongoCollection;
import io.eaglejs.ffxi.mapper.PlayerMapper;
import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.service.MongoDBService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Sorts.ascending;


@Path("/players")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Players", description = "Endpoints for retrieving player information and stats")
public class PlayerResource {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerResource.class);
    private final MongoDBService mongoDBService;

    public PlayerResource(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
    }

    // TODO: Implement player initialization endpoint
    // @POST
    // @Path("/initialize_player")
    // public Response initializePlayer(Player player) { ... }
  
    // get_players   @GET /players/get_players
    @GET
    @Path("/get_players")
    @Operation(
        summary = "Get Player List",
        description = "Returns a list of all online players (lastOnline within the last 60 seconds).",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of players retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response getPlayers() {
        try {
            // Get all online players that lastOnline is within the last 60 seconds
            long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds
            long thresholdTime = currentTime - 60; // 60 seconds ago

            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            List<Document> documents = playersCollection
                    .find(gte("lastOnline", thresholdTime))
                    .sort(ascending("playerName"))
                    .into(new ArrayList<>());

            // Convert Documents to Player objects
            List<Player> players = documents.stream()
                    .map(PlayerMapper::documentToPlayer)
                    .collect(Collectors.toList());

            return Response.ok(players).build();
        } catch (Exception e) {
            LOG.error("Error retrieving players", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving the players.")
                    .build();
        }
    }
}
