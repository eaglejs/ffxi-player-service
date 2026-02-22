package io.eaglejs.ffxi.resources;

import com.mongodb.client.MongoCollection;
import io.eaglejs.ffxi.mapper.PlayerMapper;
import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.models.SetGilRequest;
import io.eaglejs.ffxi.models.SetHppRequest;
import io.eaglejs.ffxi.models.SetJobsRequest;
import io.eaglejs.ffxi.models.SetMppRequest;
import io.eaglejs.ffxi.models.SetOnlineRequest;
import io.eaglejs.ffxi.models.SetStatusRequest;
import io.eaglejs.ffxi.models.SetTpRequest;
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

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", request.getPlayerName());
            broadcastData.put("mainJob", request.getMainJob());
            broadcastData.put("subJob", request.getSubJob());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated online status for player {} ({})", request.getPlayerId(), request.getPlayerName());

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

    @POST
    @Path("/set_gil")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Gil",
        description = "Updates a player's gil amount in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Gil updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setGil(SetGilRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getGil() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and gil are required")
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
                    set("gil", request.getGil())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player gil")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("gil", request.getGil());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated gil for player {} ({}): {}", 
                request.getPlayerId(), playerName, request.getGil());
            
            return Response.ok("Gil: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player gil for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player gil.")
                    .build();
        }
    }

    @POST
    @Path("/set_status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Engagement Status",
        description = "Updates a player's engagement status in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setStatus(SetStatusRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getStatus() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and status are required")
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
                    set("status", request.getStatus())
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
            broadcastData.put("status", request.getStatus());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated engagement status for player {} ({}): {}", 
                request.getPlayerId(), playerName, request.getStatus());
            
            return Response.ok("Status: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player status for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player status.")
                    .build();
        }
    }

    @POST
    @Path("/set_hpp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player HP Percentage",
        description = "Updates a player's HP percentage in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "HPP updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setHpp(SetHppRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getHpp() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and hpp are required")
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
                    set("hpp", request.getHpp())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player hpp")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("hpp", request.getHpp());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated hpp for player {} ({}): {}", 
                request.getPlayerId(), playerName, request.getHpp());
            
            return Response.ok("HP: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player hpp for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player hpp.")
                    .build();
        }
    }

    @POST
    @Path("/set_mpp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player MP Percentage",
        description = "Updates a player's MP percentage in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "MPP updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setMpp(SetMppRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getMpp() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and mpp are required")
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
                    set("mpp", request.getMpp())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player mpp")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("mpp", request.getMpp());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated mpp for player {} ({}): {}", 
                request.getPlayerId(), playerName, request.getMpp());
            
            return Response.ok("MP: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player mpp for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player mpp.")
                    .build();
        }
    }

    @POST
    @Path("/set_tp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player TP",
        description = "Updates a player's TP (Tactical Points) in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "TP updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setTp(SetTpRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getTp() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and tp are required")
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
                    set("tp", request.getTp())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player tp")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("tp", request.getTp());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated tp for player {} ({}): {}", 
                request.getPlayerId(), playerName, request.getTp());
            
            return Response.ok("TP: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player tp for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player tp.")
                    .build();
        }
    }
}
