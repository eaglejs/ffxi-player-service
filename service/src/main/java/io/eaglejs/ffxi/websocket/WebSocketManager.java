package io.eaglejs.ffxi.websocket;

import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.DeploymentException;

/**
 * Manages the WebSocket server lifecycle within Dropwizard.
 * Registers the PlayerWebSocket endpoint with Jetty's WebSocket container.
 */
public class WebSocketManager implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketManager.class);

    private final ServletContextHandler context;

    public WebSocketManager(ServletContextHandler context) {
        this.context = context;
        // Must be configured before the servlet context starts
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, container) -> {
            container.addEndpoint(PlayerWebSocket.class);
            LOG.info("WebSocket endpoint registered at /ws/players");
        });
    }

    @Override
    public void start() throws Exception {
        LOG.info("WebSocket manager started");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("WebSocket server stopped");
    }
}
