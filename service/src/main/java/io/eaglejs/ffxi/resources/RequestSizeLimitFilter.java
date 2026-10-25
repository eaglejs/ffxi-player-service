package io.eaglejs.ffxi.resources;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Rejects oversized request bodies for the inventory snapshot endpoint with 413,
 * based on the declared Content-Length.
 */
@Provider
@PreMatching
@Priority(1000)
public class RequestSizeLimitFilter implements ContainerRequestFilter {

  public static final long MAX_INVENTORY_BODY_BYTES = 1024 * 1024;

  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    if (!"POST".equals(context.getMethod()) || !context.getUriInfo().getPath().endsWith("player/set_inventory")) {
      return;
    }
    if (context.getLength() > MAX_INVENTORY_BODY_BYTES) {
      context.abortWith(Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
          .type("text/plain")
          .entity("Request body exceeds " + MAX_INVENTORY_BODY_BYTES + " bytes")
          .build());
    }
  }
}
