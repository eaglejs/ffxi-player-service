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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
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

    @POST
    @Path("/initialize_player")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Initialize Player",
        description = "Initializes a new player record in the database.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Player initialized successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid player data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response initializePlayer(Player player) { 
      try {
          if (player == null) {
              return Response.status(Response.Status.BAD_REQUEST)
                      .entity("Player data cannot be null.")
                      .build();
          }
          
          MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
          Document doc = PlayerMapper.playerToDocument(player);
          playersCollection.insertOne(doc);
          return Response.ok("Player initialized successfully").build();
      } catch (Exception e) {
          LOG.error("Error initializing player", e);
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity("An error occurred while initializing the player.")
                  .build();
      }
    }
  
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

    @GET
    @Path("/get_player")
    @Operation(
        summary = "Get Player by ID",
        description = "Returns player stats from the database by playerId.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Player retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "400", description = "Invalid playerId"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response getPlayer(@QueryParam("playerId") Integer playerId) {
        try {
            if (playerId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId query parameter is required")
                        .build();
            }

            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            Document document = playersCollection.find(eq("playerId", playerId)).first();

            if (document == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + playerId)
                        .build();
            }

            Player player = PlayerMapper.documentToPlayer(document);
            return Response.ok(player).build();
        } catch (Exception e) {
            LOG.error("Error retrieving player with playerId: " + playerId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving the player.")
                    .build();
        }
    }
}
