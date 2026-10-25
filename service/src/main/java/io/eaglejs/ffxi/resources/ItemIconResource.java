package io.eaglejs.ffxi.resources;

import io.eaglejs.ffxi.service.ItemIconStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/items")
@Tag(name = "Item Icons", description = "Permanent, write-once store of 32x32 item icons")
public class ItemIconResource {

  private static final Logger LOG = LoggerFactory.getLogger(ItemIconResource.class);

  static final int MAX_MISSING_IDS = 5000;
  private static final String PNG = "image/png";

  private final ItemIconStore store;

  public ItemIconResource(ItemIconStore store) {
    this.store = store;
  }

  @GET
  @Path("/icon/{id: \\d{1,9}}.png")
  @Produces(PNG)
  @Operation(summary = "Get Item Icon", description = "Serves the stored icon for an item id. Icons never change once stored, so responses are cached for a year.", responses = {
      @ApiResponse(responseCode = "200", description = "PNG icon"),
      @ApiResponse(responseCode = "404", description = "No icon stored for the item"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getIcon(@PathParam("id") String rawId) {
    try {
      Integer id = parseId(rawId);
      if (id == null) {
        return notFound();
      }
      byte[] png = store.read(id);
      if (png == null) {
        return notFound();
      }
      CacheControl cache = new CacheControl();
      cache.setMaxAge(31_536_000);
      cache.setPrivate(false);
      cache.getCacheExtension().put("immutable", null);
      return Response.ok(png, PNG).cacheControl(cache).build();
    } catch (Exception e) {
      LOG.error("Error reading item icon {}", rawId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
          .entity("An error occurred while reading the item icon.").build();
    }
  }

  @PUT
  @Path("/icon/{id: \\d{1,9}}.png")
  @Consumes(PNG)
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Upload Item Icon", description = "Stores a 32x32 PNG for an item id. Write-once: an existing icon is never overwritten.", responses = {
      @ApiResponse(responseCode = "201", description = "Icon stored"),
      @ApiResponse(responseCode = "200", description = "Icon already exists, nothing changed"),
      @ApiResponse(responseCode = "400", description = "Invalid item id or not a 32x32 PNG"),
      @ApiResponse(responseCode = "413", description = "Icon too large"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response putIcon(@PathParam("id") String rawId, InputStream body) {
    try {
      Integer id = parseId(rawId);
      if (id == null) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("item id must be between 0 and " + ItemIconStore.MAX_ITEM_ID).build();
      }

      byte[] data = readBounded(body, ItemIconStore.MAX_ICON_BYTES);
      if (data.length > ItemIconStore.MAX_ICON_BYTES) {
        return Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
            .entity("icon exceeds " + ItemIconStore.MAX_ICON_BYTES + " bytes").build();
      }
      String error = ItemIconStore.validatePng(data);
      if (error != null) {
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
      }

      if (store.storeIfAbsent(id, data)) {
        return Response.status(Response.Status.CREATED).entity("Icon: STORED").build();
      }
      return Response.ok("Icon: EXISTS").build();
    } catch (Exception e) {
      LOG.error("Error storing item icon {}", rawId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("An error occurred while storing the item icon.").build();
    }
  }

  @POST
  @Path("/icons/missing")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Find Missing Icons", description = "Given item ids, returns the ones that have no stored icon yet.", responses = {
      @ApiResponse(responseCode = "200", description = "Missing ids"),
      @ApiResponse(responseCode = "400", description = "Invalid request body"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response missing(Map<String, List<Integer>> request) {
    try {
      List<Integer> ids = request == null ? null : request.get("ids");
      if (ids == null) {
        return badJson("ids is required");
      }
      if (ids.size() > MAX_MISSING_IDS) {
        return badJson("ids must contain at most " + MAX_MISSING_IDS + " entries");
      }
      for (Integer id : ids) {
        if (id == null || !ItemIconStore.isValidItemId(id)) {
          return badJson("every id must be between 0 and " + ItemIconStore.MAX_ITEM_ID);
        }
      }
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("missing", new ArrayList<>(store.missing(ids)));
      return Response.ok(body).build();
    } catch (Exception e) {
      LOG.error("Error checking missing item icons", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
          .entity("An error occurred while checking item icons.").build();
    }
  }

  private static Integer parseId(String raw) {
    try {
      int id = Integer.parseInt(raw);
      return ItemIconStore.isValidItemId(id) ? id : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Reads at most limit+1 bytes so an oversized body is detected without buffering all of it. */
  private static byte[] readBounded(InputStream in, int limit) throws IOException {
    return in.readNBytes(limit + 1);
  }

  private static Response notFound() {
    return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("No icon stored").build();
  }

  private static Response badJson(String message) {
    return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build();
  }
}
