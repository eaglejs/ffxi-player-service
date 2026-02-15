package io.eaglejs.ffxi.websocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PlayerWebSocketTest {

    private PlayerWebSocket webSocket;

    @Before
    public void setUp() {
        PlayerWebSocket.reset();
        webSocket = new PlayerWebSocket();
    }

    @After
    public void tearDown() {
        PlayerWebSocket.reset();
    }

    @Test
    public void testOnOpenSendsWelcomeMessage() throws Exception {
        Session session = createMockSession("session-1");
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);

        webSocket.onOpen(session);

        assertEquals(1, PlayerWebSocket.getConnectedClientCount());
        verify(basicRemote).sendText(contains("Hello! I am the server!"));
    }

    @Test
    public void testOnCloseRemovesSession() throws Exception {
        Session session = createMockSession("session-1");
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);

        webSocket.onOpen(session);
        assertEquals(1, PlayerWebSocket.getConnectedClientCount());

        webSocket.onClose(session);
        assertEquals(0, PlayerWebSocket.getConnectedClientCount());
    }

    @Test
    public void testOnErrorRemovesSession() throws Exception {
        Session session = createMockSession("session-1");
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);

        webSocket.onOpen(session);
        assertEquals(1, PlayerWebSocket.getConnectedClientCount());

        webSocket.onError(session, new RuntimeException("test error"));
        assertEquals(0, PlayerWebSocket.getConnectedClientCount());
    }

    @Test
    public void testBroadcastToAllClients() throws Exception {
        Session session1 = createMockSession("session-1");
        Session session2 = createMockSession("session-2");
        RemoteEndpoint.Basic basic1 = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Basic basic2 = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Async async1 = mock(RemoteEndpoint.Async.class);
        RemoteEndpoint.Async async2 = mock(RemoteEndpoint.Async.class);
        when(session1.getBasicRemote()).thenReturn(basic1);
        when(session2.getBasicRemote()).thenReturn(basic2);
        when(session1.getAsyncRemote()).thenReturn(async1);
        when(session2.getAsyncRemote()).thenReturn(async2);
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);

        webSocket.onOpen(session1);
        webSocket.onOpen(session2);

        Map<String, Object> data = new HashMap<>();
        data.put("playerId", 123);
        data.put("playerName", "testplayer");
        data.put("hpp", 100);

        PlayerWebSocket.broadcast(data);

        verify(async1).sendText(contains("testplayer"));
        verify(async2).sendText(contains("testplayer"));
    }

    @Test
    public void testSubscriptionFiltersMessages() throws Exception {
        Session session1 = createMockSession("session-1");
        Session session2 = createMockSession("session-2");
        RemoteEndpoint.Basic basic1 = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Basic basic2 = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Async async1 = mock(RemoteEndpoint.Async.class);
        RemoteEndpoint.Async async2 = mock(RemoteEndpoint.Async.class);
        when(session1.getBasicRemote()).thenReturn(basic1);
        when(session2.getBasicRemote()).thenReturn(basic2);
        when(session1.getAsyncRemote()).thenReturn(async1);
        when(session2.getAsyncRemote()).thenReturn(async2);
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);

        webSocket.onOpen(session1);
        webSocket.onOpen(session2);

        // Session 1 subscribes to player 123 only
        webSocket.onMessage("{\"action\":\"subscribe\",\"playerId\":123}", session1);

        // Broadcast for player 456 - should only go to session2 (no subs = broadcast mode)
        Map<String, Object> data456 = new HashMap<>();
        data456.put("playerId", 456);
        data456.put("playerName", "otherplayer");
        PlayerWebSocket.broadcast(data456);

        verify(async1, never()).sendText(anyString());
        verify(async2).sendText(contains("otherplayer"));

        // Broadcast for player 123 - should go to both
        Map<String, Object> data123 = new HashMap<>();
        data123.put("playerId", 123);
        data123.put("playerName", "subscribedplayer");
        PlayerWebSocket.broadcast(data123);

        verify(async1).sendText(contains("subscribedplayer"));
        verify(async2).sendText(contains("subscribedplayer"));
    }

    @Test
    public void testUnsubscribeRestoresBroadcastMode() throws Exception {
        Session session = createMockSession("session-1");
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.getAsyncRemote()).thenReturn(async);
        when(session.isOpen()).thenReturn(true);

        webSocket.onOpen(session);

        // Subscribe, then unsubscribe
        webSocket.onMessage("{\"action\":\"subscribe\",\"playerId\":123}", session);
        webSocket.onMessage("{\"action\":\"unsubscribe\",\"playerId\":123}", session);

        // Broadcast for any player should now reach this session
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", 999);
        data.put("playerName", "anyplayer");
        PlayerWebSocket.broadcast(data);

        verify(async).sendText(contains("anyplayer"));
    }

    @Test
    public void testUnsubscribeAllRestoresBroadcastMode() throws Exception {
        Session session = createMockSession("session-1");
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.getAsyncRemote()).thenReturn(async);
        when(session.isOpen()).thenReturn(true);

        webSocket.onOpen(session);

        // Subscribe to multiple players
        webSocket.onMessage("{\"action\":\"subscribe\",\"playerId\":100}", session);
        webSocket.onMessage("{\"action\":\"subscribe\",\"playerId\":200}", session);

        // Unsubscribe all
        webSocket.onMessage("{\"action\":\"unsubscribe_all\"}", session);

        // Should receive all broadcasts again
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", 999);
        data.put("playerName", "anyplayer");
        PlayerWebSocket.broadcast(data);

        verify(async).sendText(contains("anyplayer"));
    }

    @Test
    public void testBroadcastSkipsClosedSessions() throws Exception {
        Session openSession = createMockSession("open");
        Session closedSession = createMockSession("closed");
        RemoteEndpoint.Basic basic1 = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Basic basic2 = mock(RemoteEndpoint.Basic.class);
        RemoteEndpoint.Async async1 = mock(RemoteEndpoint.Async.class);
        RemoteEndpoint.Async async2 = mock(RemoteEndpoint.Async.class);
        when(openSession.getBasicRemote()).thenReturn(basic1);
        when(closedSession.getBasicRemote()).thenReturn(basic2);
        when(openSession.getAsyncRemote()).thenReturn(async1);
        when(closedSession.getAsyncRemote()).thenReturn(async2);
        when(openSession.isOpen()).thenReturn(true);
        when(closedSession.isOpen()).thenReturn(false);

        webSocket.onOpen(openSession);
        webSocket.onOpen(closedSession);

        Map<String, Object> data = new HashMap<>();
        data.put("playerId", 1);
        data.put("playerName", "test");
        PlayerWebSocket.broadcast(data);

        verify(async1).sendText(anyString());
        verify(async2, never()).sendText(anyString());
    }

    @Test
    public void testBroadcastWithNoSessions() {
        // Should not throw
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", 1);
        data.put("playerName", "test");
        PlayerWebSocket.broadcast(data);
    }

    @Test
    public void testMultipleClientsConnectDisconnect() throws Exception {
        Session s1 = createMockSession("s1");
        Session s2 = createMockSession("s2");
        Session s3 = createMockSession("s3");
        when(s1.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        when(s2.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        when(s3.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));

        webSocket.onOpen(s1);
        webSocket.onOpen(s2);
        webSocket.onOpen(s3);
        assertEquals(3, PlayerWebSocket.getConnectedClientCount());

        webSocket.onClose(s2);
        assertEquals(2, PlayerWebSocket.getConnectedClientCount());
        assertTrue(PlayerWebSocket.getSessions().contains(s1));
        assertFalse(PlayerWebSocket.getSessions().contains(s2));
        assertTrue(PlayerWebSocket.getSessions().contains(s3));
    }

    private Session createMockSession(String id) {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(id);
        return session;
    }

    private static String contains(String substring) {
        return argThat(s -> s != null && s.contains(substring));
    }
}
