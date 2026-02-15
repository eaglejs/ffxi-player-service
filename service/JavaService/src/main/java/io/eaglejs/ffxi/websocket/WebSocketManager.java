package io.eaglejs.ffxi.websocket;

import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.server.ServerContainer;

/**
 * Manages the WebSocket server lifecycle within Dropwizard.
 * Registers the PlayerWebSocket endpoint with Jetty's WebSocket container.
 */
public class WebSocketManager implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketManager.class);

    private final ServletContextHandler context;

    public WebSocketManager(ServletContextHandler context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
        ServerContainer container = WebSocketServerContainerInitializer.initialize(context);
        container.addEndpoint(PlayerWebSocket.class);
        LOG.info("WebSocket endpoint registered at /ws/players");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("WebSocket server stopped");
    }
}
