package io.eaglejs.ffxi.resources;

import com.mongodb.client.MongoCollection;
import io.eaglejs.ffxi.mapper.PlayerMapper;
import io.eaglejs.ffxi.models.Currency1;
import io.eaglejs.ffxi.models.Currency2;
import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.models.RefreshBuffsRequest;
import io.eaglejs.ffxi.models.ResetExpHistoryRequest;
import io.eaglejs.ffxi.models.SetBuffsRequest;
import io.eaglejs.ffxi.models.SetCapacityPointsRequest;
import io.eaglejs.ffxi.models.SetCurrency1Request;
import io.eaglejs.ffxi.models.SetCurrency2Request;
import io.eaglejs.ffxi.models.SetGilRequest;
import io.eaglejs.ffxi.models.SetHppRequest;
import io.eaglejs.ffxi.models.SetJobsRequest;
import io.eaglejs.ffxi.models.SetMeritsRequest;
import io.eaglejs.ffxi.models.SetMessagesRequest;
import io.eaglejs.ffxi.models.SetMppRequest;
import io.eaglejs.ffxi.models.SetOnlineRequest;
import io.eaglejs.ffxi.models.SetStatsRequest;
import io.eaglejs.ffxi.models.SetStatusRequest;
import io.eaglejs.ffxi.models.SetTpRequest;
import io.eaglejs.ffxi.models.SetZoneRequest;
import io.eaglejs.ffxi.models.Stats;
import io.eaglejs.ffxi.service.MongoDBService;
import io.eaglejs.ffxi.websocket.PlayerWebSocket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @POST
    @Path("/set_zone")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Zone",
        description = "Updates a player's current zone in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Zone updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setZone(SetZoneRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getZone() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and zone are required")
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
                    set("zone", request.getZone())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player zone")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("zone", request.getZone());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated zone for player {} ({}): {}", 
                request.getPlayerId(), playerName, request.getZone());
            
            return Response.ok("Zone: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player zone for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player zone.")
                    .build();
        }
    }

    @POST
    @Path("/set_merits")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Merits",
        description = "Updates a player's merit points (total and max) in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Merits updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setMerits(SetMeritsRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getTotal() == null || 
                request.getMax() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, total, and max are required")
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

            Document meritsDoc = new Document();
            meritsDoc.put("total", request.getTotal());
            meritsDoc.put("max", request.getMax());

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("merits", meritsDoc)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player merits")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            
            Map<String, Object> meritsData = new HashMap<>();
            meritsData.put("total", request.getTotal());
            meritsData.put("max", request.getMax());
            broadcastData.put("merits", meritsData);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated merits for player {} ({}): total={}, max={}", 
                request.getPlayerId(), playerName, request.getTotal(), request.getMax());
            
            return Response.ok("Merits: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player merits for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player merits.")
                    .build();
        }
    }

    @POST
    @Path("/set_capacity_points")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Capacity Points",
        description = "Updates a player's capacity points in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Capacity points updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setCapacityPoints(SetCapacityPointsRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getTotal() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and total are required")
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

            Document capacityPointsDoc = new Document();
            capacityPointsDoc.put("total", request.getTotal());

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("capacityPoints", capacityPointsDoc)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player capacity points")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            
            Map<String, Object> capacityPointsData = new HashMap<>();
            capacityPointsData.put("total", request.getTotal());
            broadcastData.put("capacityPoints", capacityPointsData);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated capacity points for player {} ({}): total={}", 
                request.getPlayerId(), playerName, request.getTotal());
            
            return Response.ok("Capacity points: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player capacity points for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player capacity points.")
                    .build();
        }
    }

    @GET
    @Path("/get_buffs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get Player Buffs",
        description = "Retrieves a player's current buffs from the database by playerId.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Buffs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response getBuffs(@QueryParam("playerId") Integer playerId) {
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

            // Extract buffs array from the document, return empty array if not present
            List<Integer> buffs = document.get("buffs", List.class);
            if (buffs == null) {
                buffs = new ArrayList<>();
            }

            return Response.ok(buffs).build();
        } catch (Exception e) {
            LOG.error("Error retrieving buffs for playerId: " + playerId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving player buffs.")
                    .build();
        }
    }

    @POST
    @Path("/set_buffs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Buffs",
        description = "Updates a player's buffs in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Buffs updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setBuffs(SetBuffsRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getBuffs() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and buffs are required")
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
                    set("buffs", request.getBuffs())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player buffs")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("buffs", request.getBuffs());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated buffs for player {} ({}): {} buffs", 
                request.getPlayerId(), playerName, request.getBuffs().size());
            
            return Response.ok("Buffs: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player buffs for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player buffs.")
                    .build();
        }
    }

    @GET
    @Path("/get_chat_log")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get Player Chat Log",
        description = "Retrieves a player's chat log from the database by playerId.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Chat log retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response getChatLog(@QueryParam("playerId") Integer playerId) {
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

            // Extract chatLog array from the document, return empty array if not present
            List<Document> chatLog = document.get("chatLog", List.class);
            if (chatLog == null) {
                chatLog = new ArrayList<>();
            }

            return Response.ok(chatLog).build();
        } catch (Exception e) {
            LOG.error("Error retrieving chat log for playerId: " + playerId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving player chat log.")
                    .build();
        }
    }

    @GET
    @Path("/get_chat_log_by_type")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get Player Chat Log by Type",
        description = "Retrieves a player's chat log filtered by message type from the database by playerId.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Filtered chat log retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response getChatLogByType(
            @QueryParam("playerId") Integer playerId,
            @QueryParam("messageType") Integer messageType) {
        try {
            if (playerId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId query parameter is required")
                        .build();
            }

            if (messageType == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("messageType query parameter is required")
                        .build();
            }

            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            Document document = playersCollection.find(eq("playerId", playerId)).first();

            if (document == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + playerId)
                        .build();
            }

            // Extract chatLog array from the document
            List<Document> chatLog = document.get("chatLog", List.class);
            if (chatLog == null) {
                chatLog = new ArrayList<>();
            }

            // Filter by messageType
            List<Document> filteredChatLog = new ArrayList<>();
            for (Document message : chatLog) {
                Integer msgType = message.getInteger("messageType");
                if (msgType != null && msgType.equals(messageType)) {
                    filteredChatLog.add(message);
                }
            }

            return Response.ok(filteredChatLog).build();
        } catch (Exception e) {
            LOG.error("Error retrieving chat log by type for playerId: " + playerId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving player chat log by type.")
                    .build();
        }
    }

    @POST
    @Path("/set_messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Messages",
        description = "Updates a player's messages in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Messages updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setMessages(SetMessagesRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getMessagesPackage() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and messagesPackage are required")
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
                    set("chatLog", request.getMessagesPackage())
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player messages")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("messagesPackage", request.getMessagesPackage());
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated messages for player {} ({}): {} messages", 
                request.getPlayerId(), playerName, request.getMessagesPackage().size());
            
            return Response.ok("Messages: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player messages for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player messages.")
                    .build();
        }
    }

    @POST
    @Path("/refresh_buffs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Refresh Player Buffs",
        description = "Refreshes (clears) a player's buffs in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Buffs refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response refreshBuffs(RefreshBuffsRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || request.getPlayerName() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId and playerName are required")
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

            // Refresh buffs by setting to empty array
            List<Integer> emptyBuffs = new ArrayList<>();

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("buffs", emptyBuffs)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to refresh player buffs")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("buffs", emptyBuffs);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Refreshed buffs for player {} ({})", 
                request.getPlayerId(), playerName);
            
            return Response.ok("Buffs refreshed: OK").build();
        } catch (Exception e) {
            LOG.error("Error refreshing player buffs for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while refreshing player buffs.")
                    .build();
        }
    }

    @POST
    @Path("/reset_exp_history")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Reset Player Experience History",
        description = "Resets a player's experience history by clearing all exp types to empty arrays in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Experience history reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response resetExpHistory(ResetExpHistoryRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || request.getPlayerName() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId and playerName are required")
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

            // Reset exp history by setting all types to empty arrays
            Document expHistory = new Document();
            expHistory.put("experience", new ArrayList<>());
            expHistory.put("capacity", new ArrayList<>());
            expHistory.put("exemplar", new ArrayList<>());

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("expHistory", expHistory)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to reset player exp history")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            
            Map<String, Object> expHistoryData = new HashMap<>();
            expHistoryData.put("experience", new ArrayList<>());
            expHistoryData.put("capacity", new ArrayList<>());
            expHistoryData.put("exemplar", new ArrayList<>());
            broadcastData.put("expHistory", expHistoryData);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Reset exp history for player {} ({})", 
                request.getPlayerId(), playerName);
            
            return Response.ok("Experience history reset: OK").build();
        } catch (Exception e) {
            LOG.error("Error resetting player exp history for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while resetting player exp history.")
                    .build();
        }
    }

    @POST
    @Path("/set_currency1")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Currency1",
        description = "Updates a player's currency1 values in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Currency1 updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setCurrency1(SetCurrency1Request request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getCurrency1() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and currency1 are required")
                        .build();
            }

            String playerName = request.getPlayerName().toLowerCase();
            Currency1 c1 = request.getCurrency1();
            
            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            
            Document existingPlayer = playersCollection.find(eq("playerId", request.getPlayerId())).first();
            if (existingPlayer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + request.getPlayerId())
                        .build();
            }

            Document currency1Doc = new Document();
            currency1Doc.put("conquestPointsBastok", c1.getConquestPointsBastok());
            currency1Doc.put("conquestPointsSandoria", c1.getConquestPointsSandoria());
            currency1Doc.put("conquestPointsWindurst", c1.getConquestPointsWindurst());
            currency1Doc.put("deeds", c1.getDeeds());
            currency1Doc.put("dominionNotes", c1.getDominionNotes());
            currency1Doc.put("imperialStanding", c1.getImperialStanding());
            currency1Doc.put("loginPoints", c1.getLoginPoints());
            currency1Doc.put("nyzulTokens", c1.getNyzulTokens());
            currency1Doc.put("sparksOfEminence", c1.getSparksOfEminence());
            currency1Doc.put("therionIchor", c1.getTherionIchor());
            currency1Doc.put("unityAccolades", c1.getUnityAccolades());
            currency1Doc.put("voidstones", c1.getVoidstones());

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("currency1", currency1Doc)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player currency1")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("currency1", currency1Doc);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated currency1 for player {} ({})", 
                request.getPlayerId(), playerName);
            
            return Response.ok("Currency1: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player currency1 for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player currency1.")
                    .build();
        }
    }

    @POST
    @Path("/set_currency2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Currency2",
        description = "Updates a player's currency2 values in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Currency2 updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setCurrency2(SetCurrency2Request request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getCurrency2() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and currency2 are required")
                        .build();
            }

            String playerName = request.getPlayerName().toLowerCase();
            Currency2 c2 = request.getCurrency2();
            
            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            
            Document existingPlayer = playersCollection.find(eq("playerId", request.getPlayerId())).first();
            if (existingPlayer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + request.getPlayerId())
                        .build();
            }

            Document currency2Doc = new Document();
            currency2Doc.put("domainPoints", c2.getDomainPoints());
            currency2Doc.put("eschaBeads", c2.getEschaBeads());
            currency2Doc.put("eschaSilt", c2.getEschaSilt());
            currency2Doc.put("gallantry", c2.getGallantry());
            currency2Doc.put("gallimaufry", c2.getGallimaufry());
            currency2Doc.put("hallmarks", c2.getHallmarks());
            currency2Doc.put("mogSegments", c2.getMogSegments());
            currency2Doc.put("mweyaPlasmCorpuscles", c2.getMweyaPlasmCorpuscles());
            currency2Doc.put("potpourri", c2.getPotpourri());
            currency2Doc.put("coalitionImprimaturs", c2.getCoalitionImprimaturs());
            currency2Doc.put("temenosUnits", c2.getTemenosUnits());
            currency2Doc.put("apollyonUnits", c2.getApollyonUnits());

            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("currency2", currency2Doc)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player currency2")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("currency2", currency2Doc);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated currency2 for player {} ({})", 
                request.getPlayerId(), playerName);
            
            return Response.ok("Currency2: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player currency2 for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player currency2.")
                    .build();
        }
    }

    @POST
    @Path("/set_stats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Set Player Stats",
        description = "Updates a player's stats (both root-level fields and nested stats object) in the database and broadcasts the update via WebSocket.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Stats updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Response setStats(SetStatsRequest request) {
        try {
            if (request == null || request.getPlayerId() == null || 
                request.getPlayerName() == null || request.getStats() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("playerId, playerName, and stats are required")
                        .build();
            }

            String playerName = request.getPlayerName().toLowerCase();
            Stats s = request.getStats();
            
            MongoCollection<Document> playersCollection = mongoDBService.getPlayersCollection();
            
            Document existingPlayer = playersCollection.find(eq("playerId", request.getPlayerId())).first();
            if (existingPlayer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Player not found with playerId: " + request.getPlayerId())
                        .build();
            }

            // Create stats document
            Document statsDoc = new Document();
            statsDoc.put("baseSTR", s.getBaseSTR());
            statsDoc.put("baseAGI", s.getBaseAGI());
            statsDoc.put("baseDEX", s.getBaseDEX());
            statsDoc.put("baseVIT", s.getBaseVIT());
            statsDoc.put("baseINT", s.getBaseINT());
            statsDoc.put("baseMND", s.getBaseMND());
            statsDoc.put("baseCHR", s.getBaseCHR());
            statsDoc.put("addedSTR", s.getAddedSTR());
            statsDoc.put("addedAGI", s.getAddedAGI());
            statsDoc.put("addedDEX", s.getAddedDEX());
            statsDoc.put("addedVIT", s.getAddedVIT());
            statsDoc.put("addedINT", s.getAddedINT());
            statsDoc.put("addedMND", s.getAddedMND());
            statsDoc.put("addedCHR", s.getAddedCHR());
            statsDoc.put("fireResistance", s.getFireResistance());
            statsDoc.put("iceResistance", s.getIceResistance());
            statsDoc.put("windResistance", s.getWindResistance());
            statsDoc.put("earthResistance", s.getEarthResistance());
            statsDoc.put("lightningResistance", s.getLightningResistance());
            statsDoc.put("waterResistance", s.getWaterResistance());
            statsDoc.put("lightResistance", s.getLightResistance());
            statsDoc.put("darkResistance", s.getDarkResistance());

            // Update both root-level fields and nested stats object
            com.mongodb.client.result.UpdateResult result = playersCollection.updateOne(
                eq("playerId", request.getPlayerId()),
                combine(
                    set("playerName", playerName),
                    set("masterLevel", request.getMasterLevel()),
                    set("mainJobLevel", request.getMainJobLevel()),
                    set("subJobLevel", request.getSubJobLevel()),
                    set("attack", request.getAttack()),
                    set("defense", request.getDefense()),
                    set("title", request.getTitle()),
                    set("nationRank", request.getNationRank()),
                    set("currentExemplar", request.getCurrentExemplar()),
                    set("requiredExemplar", request.getRequiredExemplar()),
                    set("stats", statsDoc)
                )
            );

            if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update player stats")
                        .build();
            }

            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("playerId", request.getPlayerId());
            broadcastData.put("playerName", playerName);
            broadcastData.put("masterLevel", request.getMasterLevel());
            broadcastData.put("mainJobLevel", request.getMainJobLevel());
            broadcastData.put("subJobLevel", request.getSubJobLevel());
            broadcastData.put("attack", request.getAttack());
            broadcastData.put("defense", request.getDefense());
            broadcastData.put("title", request.getTitle());
            broadcastData.put("nationRank", request.getNationRank());
            broadcastData.put("currentExemplar", request.getCurrentExemplar());
            broadcastData.put("requiredExemplar", request.getRequiredExemplar());
            broadcastData.put("stats", statsDoc);
            
            PlayerWebSocket.broadcast(broadcastData);
            
            LOG.info("Updated stats for player {} ({})", 
                request.getPlayerId(), playerName);
            
            return Response.ok("Stats: OK").build();
        } catch (Exception e) {
            LOG.error("Error setting player stats for playerId: " + request.getPlayerId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while updating player stats.")
                    .build();
        }
    }
}
