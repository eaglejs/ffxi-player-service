package io.eaglejs.ffxi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * WebSocket endpoint for broadcasting player stats updates to connected clients.
 * Clients can subscribe to specific player IDs to filter which updates they receive.
 * If no subscriptions are set, the client receives all updates (broadcast mode).
 */
@ServerEndpoint("/ws/players")
public class PlayerWebSocket {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerWebSocket.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // All connected sessions
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    // Per-session subscriptions: session -> set of subscribed playerIds
    private static final Map<Session, Set<Integer>> subscriptions = new ConcurrentHashMap<>();

    // Heartbeat scheduler
    private static final ScheduledExecutorService heartbeatExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ws-heartbeat");
                t.setDaemon(true);
                return t;
            });

    private static ScheduledFuture<?> heartbeatTask;

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        subscriptions.put(session, ConcurrentHashMap.newKeySet());
        LOG.info("WebSocket client connected: {}", session.getId());

        try {
            String welcome = MAPPER.writeValueAsString(
                    Collections.singletonMap("msg", "Hello! I am the server!"));
            session.getBasicRemote().sendText(welcome);
        } catch (IOException e) {
            LOG.error("Failed to send welcome message", e);
        }

        startHeartbeatIfNeeded();
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        subscriptions.remove(session);
        LOG.info("WebSocket client disconnected: {}", session.getId());

        if (sessions.isEmpty()) {
            stopHeartbeat();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage());
        sessions.remove(session);
        subscriptions.remove(session);
    }

    /**
     * Handles incoming messages from clients.
     * Supported message types:
     * - {"action": "subscribe", "playerId": 123} - Subscribe to a specific player's updates
     * - {"action": "unsubscribe", "playerId": 123} - Unsubscribe from a specific player's updates
     * - {"action": "unsubscribe_all"} - Unsubscribe from all (receive all updates)
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // Handle plain-text ping/pong (non-JSON heartbeat responses)
            String trimmed = message.trim();
            if ("pong".equalsIgnoreCase(trimmed)) {
                LOG.debug("Received pong from session {}", session.getId());
                return;
            }
            if ("ping".equalsIgnoreCase(trimmed)) {
                session.getBasicRemote().sendText("pong");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> msg = MAPPER.readValue(message, Map.class);
            String action = (String) msg.get("action");

            if (action == null) {
                return;
            }

            switch (action) {
                case "ping": {
                    // Client heartbeat - respond with pong
                    session.getBasicRemote().sendText(MAPPER.writeValueAsString(
                            Collections.singletonMap("action", "pong")));
                    break;
                }
                case "subscribe": {
                    Object playerIdObj = msg.get("playerId");
                    if (playerIdObj instanceof Number) {
                        int playerId = ((Number) playerIdObj).intValue();
                        Set<Integer> subs = subscriptions.get(session);
                        if (subs != null) {
                            subs.add(playerId);
                            LOG.info("Session {} subscribed to player {}", session.getId(), playerId);
                            sendAck(session, "subscribed", playerId);
                        }
                    }
                    break;
                }
                case "close": {
                    Object playerIdObj = msg.get("playerId");
                    if (playerIdObj instanceof Number) {
                        int playerId = ((Number) playerIdObj).intValue();
                        Set<Integer> subs = subscriptions.get(session);
                        if (subs != null) {
                            subs.remove(playerId);
                            LOG.info("Session {} unsubscribed from player {}", session.getId(), playerId);
                            sendAck(session, "unsubscribed", playerId);
                        }
                    }
                    break;
                }
                case "unsubscribe_all": {
                    Set<Integer> subs = subscriptions.get(session);
                    if (subs != null) {
                        subs.clear();
                        LOG.info("Session {} unsubscribed from all players", session.getId());
                        sendAck(session, "unsubscribed_all", null);
                    }
                    break;
                }
                default:
                    LOG.warn("Unknown action: {}", action);
                    break;
            }
        } catch (Exception e) {
            LOG.error("Failed to process message from session {}", session.getId(), e);
        }
    }

    /**
     * Broadcasts a player stats update to all connected clients.
     * Clients with active subscriptions only receive updates for their subscribed players.
     * Clients with no subscriptions receive all updates.
     */
    public static void broadcast(Map<String, Object> data) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            String json = MAPPER.writeValueAsString(data);
            Object playerIdObj = data.get("playerId");
            Integer playerId = (playerIdObj instanceof Number) ? ((Number) playerIdObj).intValue() : null;

            for (Session session : sessions) {
                if (session.isOpen()) {
                    if (shouldSendToSession(session, playerId)) {
                        try {
                            session.getAsyncRemote().sendText(json);
                        } catch (Exception e) {
                            LOG.error("Failed to send message to session {}", session.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to broadcast message", e);
        }
    }

    private static boolean shouldSendToSession(Session session, Integer playerId) {
        Set<Integer> subs = subscriptions.get(session);
        // No subscriptions = receive everything (broadcast mode)
        if (subs == null || subs.isEmpty()) {
            return true;
        }
        // Has subscriptions = only receive matching player updates
        return playerId != null && subs.contains(playerId);
    }

    private void sendAck(Session session, String status, Integer playerId) {
        try {
            Map<String, Object> ack = new ConcurrentHashMap<>();
            ack.put("action", status);
            if (playerId != null) {
                ack.put("playerId", playerId);
            }
            session.getBasicRemote().sendText(MAPPER.writeValueAsString(ack));
        } catch (IOException e) {
            LOG.error("Failed to send ack to session {}", session.getId(), e);
        }
    }

    private synchronized void startHeartbeatIfNeeded() {
        if (heartbeatTask == null || heartbeatTask.isCancelled()) {
            heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(
                    PlayerWebSocket::sendHeartbeats, 30, 30, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
    }

    private static void sendHeartbeats() {
        ByteBuffer pingData = ByteBuffer.wrap("ping".getBytes());
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendPing(pingData);
                } catch (Exception e) {
                    LOG.warn("Failed to send ping to session {}", session.getId());
                }
            }
        }
    }

    public static int getConnectedClientCount() {
        return sessions.size();
    }

    public static Set<Session> getSessions() {
        return Collections.unmodifiableSet(sessions);
    }

    // For testing: clear all state
    static void reset() {
        sessions.clear();
        subscriptions.clear();
    }
}
