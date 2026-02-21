package io.eaglejs.ffxi.resources;

import com.mongodb.client.MongoCollection;
import io.eaglejs.ffxi.mapper.PlayerMapper;
import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.models.SetJobsRequest;
import io.eaglejs.ffxi.models.SetOnlineRequest;
import io.eaglejs.ffxi.service.MongoDBService;
import io.eaglejs.ffxi.websocket.PlayerWebSocket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Path("/player")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Player", description = "Endpoints for individual player operations")
public class SinglePlayerResource {

    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerResource.class);
    private final MongoDBService mongoDBService;

    public SinglePlayerResource(MongoDBService mongoDBService) {
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

    @POST
    @Path("/set_online")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Online Status",
        description = "Updates a player's online status in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Player status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setOnline(SetOnlineRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getLastOnline() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and lastOnline are required")
                        .build();
            }

            String playerName = request.getPlayerName().toLowerCase();
            
            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            
            Document existingPlayer = playersCollection.find(eq("playerId", request.getPlayerId())).first();
            if (existingPlayer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + request.getPlayerId())
                        .build();
            }

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("lastOnline", request.getLastOnline())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player status")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("lastOnline", request.getLastOnline());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated online status for player {} ({})", request.getPlayerId(), playerName);
            
            return Response.ok("Player Status: Online").build();
        } catch (Exception e) {
            LOG.error("Error setting player online status for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player status.")
                    .build();
        }
    }

    @POST
    @Path("/set_jobs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Jobs",
        description = "Updates a player's main and sub jobs in the database.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Jobs updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setJobs(SetJobsRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getMainJob() == null || 
                request.getSubJob() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, mainJob, and subJob are required")
                        .build();
            }

            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            
            Document existingPlayer = playersCollection.find(eq("playerId", request.getPlayerId())).first();
            if (existingPlayer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + request.getPlayerId())
                        .build();
            }

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", request.getPlayerName()),
                    set("mainJob", request.getMainJob()),
                    set("subJob", request.getSubJob())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player jobs")
                        .build();
            }

            LOG.info("Updated jobs for player {} ({}): {}/{}", 
                request.getPlayerId(), request.getPlayerName(), 
                request.getMainJob(), request.getSubJob());
            
            return Response.ok("Jobs: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player jobs for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player jobs.")
                    .build();
        }
    }
}
