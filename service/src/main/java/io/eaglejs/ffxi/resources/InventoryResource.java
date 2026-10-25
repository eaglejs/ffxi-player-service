package io.eaglejs.ffxi.resources;

import io.eaglejs.ffxi.dao.InventoryDao;
import io.eaglejs.ffxi.dao.InventoryDao.SaveResult;
import io.eaglejs.ffxi.dao.InventoryDao.SearchCriteria;
import io.eaglejs.ffxi.dao.InventoryDao.SearchPage;
import io.eaglejs.ffxi.models.InventorySnapshotRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/player")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory", description = "Endpoints for storing and searching character inventory snapshots")
public class InventoryResource {

  private static final Logger LOG = LoggerFactory.getLogger(InventoryResource.class);

  static final int DEFAULT_LIMIT = 100;
  static final int MAX_LIMIT = 500;

  private final InventoryDao inventoryDao;

  public InventoryResource(InventoryDao inventoryDao) {
    this.inventoryDao = inventoryDao;
  }

  @POST
  @Path("/set_inventory")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Set Player Inventory", description = "Replaces a character's full inventory snapshot (gil, every bag, key items). Idempotent: an unchanged contentHash or an older timestamp is a no-op.", responses = {
      @ApiResponse(responseCode = "200", description = "Inventory saved, unchanged, or stale (ignored)"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "413", description = "Request body too large"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setInventory(InventorySnapshotRequest request) {
    try {
      if (request == null) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("Request body is null")
            .build();
      }

      List<String> errors = request.validate();
      if (!errors.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("Invalid inventory request: " + String.join("; ", errors))
            .build();
      }

      SaveResult result = inventoryDao.save(request);
      switch (result) {
        case STALE:
          return Response.ok("Inventory: STALE").build();
        case UNCHANGED:
          return Response.ok("Inventory: OK").build();
        default:
          return Response.ok("Inventory: OK").build();
      }
    } catch (Exception e) {
      LOG.error("Error setting inventory for playerId: " + (request != null ? request.getPlayerId() : "unknown"), e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("An error occurred while updating player inventory.")
          .build();
    }
  }

  @GET
  @Path("/{playerId}/inventory")
  @Operation(summary = "Get Player Inventory", description = "Returns the stored inventory snapshot for a player.", responses = {
      @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "No inventory stored for the player"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getInventory(@PathParam("playerId") int playerId) {
    try {
      Document document = inventoryDao.findByPlayerId(playerId);
      if (document == null) {
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.TEXT_PLAIN)
            .entity("No inventory found for playerId: " + playerId)
            .build();
      }

      Map<String, Object> body = new LinkedHashMap<>(document);
      body.remove("_id");
      body.remove("playerNameLower");
      for (Map.Entry<String, Object> entry : body.entrySet()) {
        if (entry.getValue() instanceof Date) {
          entry.setValue(((Date) entry.getValue()).toInstant().toString());
        }
      }
      return Response.ok(body).build();
    } catch (Exception e) {
      LOG.error("Error retrieving inventory for playerId: " + playerId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.TEXT_PLAIN)
          .entity("An error occurred while retrieving player inventory.")
          .build();
    }
  }

  @GET
  @Path("/inventory/characters")
  @Operation(summary = "List Characters With Inventories", description = "Returns every character that has a stored inventory snapshot (online or not), sorted by name.", responses = {
      @ApiResponse(responseCode = "200", description = "Characters retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response listCharacters() {
    try {
      return Response.ok(inventoryDao.listCharacters()).build();
    } catch (Exception e) {
      LOG.error("Error listing inventory characters", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.TEXT_PLAIN)
          .entity("An error occurred while listing characters.")
          .build();
    }
  }

  @GET
  @Path("/inventory/search")
  @Operation(summary = "Search Inventories", description = "Finds item ids across stored inventories, for one player or all players. Item id to name resolution is client-side.", responses = {
      @ApiResponse(responseCode = "200", description = "Search results"),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
      @ApiResponse(responseCode = "404", description = "Specified player not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response searchInventories(
      @QueryParam("item") List<String> item,
      @QueryParam("name") String name,
      @QueryParam("player") String player,
      @QueryParam("bag") List<String> bag,
      @QueryParam("includeKeyItems") boolean includeKeyItems,
      @QueryParam("limit") Integer limit,
      @QueryParam("offset") Integer offset) {
    return search(item, name, player, bag, includeKeyItems, limit, offset);
  }

  @GET
  @Path("/{playerId}/inventory/search")
  @Operation(summary = "Search One Player's Inventory", description = "Alias of /player/inventory/search with the player filter set to the path playerId.", responses = {
      @ApiResponse(responseCode = "200", description = "Search results"),
      @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
      @ApiResponse(responseCode = "404", description = "Player not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response searchPlayerInventory(
      @PathParam("playerId") int playerId,
      @QueryParam("item") List<String> item,
      @QueryParam("name") String name,
      @QueryParam("bag") List<String> bag,
      @QueryParam("includeKeyItems") boolean includeKeyItems,
      @QueryParam("limit") Integer limit,
      @QueryParam("offset") Integer offset) {
    return search(item, name, String.valueOf(playerId), bag, includeKeyItems, limit, offset);
  }

  private Response search(List<String> item, String name, String player, List<String> bag,
      boolean includeKeyItems, Integer limit, Integer offset) {
    try {
      if (name != null && !name.isBlank()) {
        return badRequest("name search is not supported: the backend only stores item ids. Resolve the name to item ids client-side and pass item=<id>");
      }

      SearchCriteria criteria = new SearchCriteria();
      String itemError = parseItemIds(item, criteria.itemIds);
      if (itemError != null) {
        return badRequest(itemError);
      }
      if (criteria.itemIds.isEmpty()) {
        return badRequest("item is required (one or more item ids)");
      }

      int effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
      int effectiveOffset = offset != null ? offset : 0;
      if (effectiveLimit < 1 || effectiveLimit > MAX_LIMIT) {
        return badRequest("limit must be between 1 and " + MAX_LIMIT);
      }
      if (effectiveOffset < 0) {
        return badRequest("offset must be >= 0");
      }
      criteria.limit = effectiveLimit;
      criteria.offset = effectiveOffset;
      criteria.includeKeyItems = includeKeyItems;

      if (bag != null) {
        for (String b : bag) {
          if (b != null && !b.isBlank()) {
            criteria.bags.add(b);
          }
        }
      }

      if (player != null && !player.isBlank()) {
        String trimmed = player.trim();
        if (trimmed.matches("\\d{1,9}")) {
          criteria.playerId = Integer.parseInt(trimmed);
        } else {
          criteria.playerNameLower = trimmed.toLowerCase();
        }
        if (!inventoryDao.playerExists(criteria)) {
          return Response.status(Response.Status.NOT_FOUND)
              .type(MediaType.TEXT_PLAIN)
              .entity("Player not found: " + trimmed)
              .build();
        }
      }

      SearchPage page = inventoryDao.search(criteria);

      Map<String, Object> echoed = new LinkedHashMap<>();
      echoed.put("item", criteria.itemIds);
      echoed.put("player", player != null && !player.isBlank() ? player.trim() : null);
      echoed.put("bag", criteria.bags);
      echoed.put("includeKeyItems", includeKeyItems);
      echoed.put("limit", effectiveLimit);
      echoed.put("offset", effectiveOffset);

      Map<String, Object> body = new LinkedHashMap<>();
      body.put("query", echoed);
      body.put("totalMatches", page.totalMatches);
      body.put("results", page.results);
      return Response.ok(body).build();
    } catch (Exception e) {
      LOG.error("Error searching inventories", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.TEXT_PLAIN)
          .entity("An error occurred while searching inventories.")
          .build();
    }
  }

  /** Parses repeated and/or comma-separated item ids; returns an error message or null. */
  static String parseItemIds(List<String> raw, List<Integer> out) {
    if (raw == null) {
      return null;
    }
    for (String value : raw) {
      if (value == null) {
        continue;
      }
      for (String part : value.split(",")) {
        String trimmed = part.trim();
        if (trimmed.isEmpty()) {
          continue;
        }
        try {
          int id = Integer.parseInt(trimmed);
          if (id < 0) {
            return "item ids must be non-negative: " + trimmed;
          }
          if (!out.contains(id)) {
            out.add(id);
          }
        } catch (NumberFormatException e) {
          return "item must be an integer id: " + trimmed;
        }
      }
    }
    return null;
  }

  private static Response badRequest(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.TEXT_PLAIN)
        .entity(message)
        .build();
  }
}
