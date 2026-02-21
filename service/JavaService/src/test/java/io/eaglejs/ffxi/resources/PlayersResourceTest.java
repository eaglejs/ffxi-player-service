package io.eaglejs.ffxi.resources;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.service.MongoDBService;
import io.eaglejs.ffxi.websocket.PlayerWebSocket;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlayersResourceTest {

    private MongoDBService mockMongoService;
    private MongoCollection<Document> mockCollection;
    private FindIterable<Document> mockFindIterable;
    private PlayersResource resource;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockMongoService = mock(MongoDBService.class);
        mockCollection = (MongoCollection<Document>) mock(MongoCollection.class);
        mockFindIterable = (FindIterable<Document>) mock(FindIterable.class);
        resource = new PlayersResource(mockMongoService);

        when(mockMongoService.getPlayersCollection()).thenReturn(mockCollection);
        
        PlayerWebSocket.reset();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPlayers_ReturnsOnlinePlayers() {
        // Arrange
        Document player1 = new Document("playerId", 1)
                .append("playerName", "Alice")
                .append("lastOnline", System.currentTimeMillis() / 1000 - 30);
        Document player2 = new Document("playerId", 2)
                .append("playerName", "Bob")
                .append("lastOnline", System.currentTimeMillis() / 1000 - 45);

        List<Document> expectedDocuments = Arrays.asList(player1, player2);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> {
            List<Document> list = invocation.getArgument(0);
            list.addAll(expectedDocuments);
            return list;
        });

        // Act
        Response response = resource.getPlayers();

        // Assert
        assertEquals(200, response.getStatus());
        List<Player> actualPlayers = (List<Player>) response.getEntity();
        assertEquals(2, actualPlayers.size());
        assertEquals("Alice", actualPlayers.get(0).getPlayerName());
        assertEquals("Bob", actualPlayers.get(1).getPlayerName());

        verify(mockCollection).find(any(Bson.class));
        verify(mockFindIterable).sort(any(Bson.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPlayers_ReturnsEmptyListWhenNoPlayersOnline() {
        // Arrange
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Response response = resource.getPlayers();

        // Assert
        assertEquals(200, response.getStatus());
        List<Player> actualPlayers = (List<Player>) response.getEntity();
        assertNotNull(actualPlayers);
        assertEquals(0, actualPlayers.size());
    }

    @Test
    public void testGetPlayers_Returns500OnDatabaseError() {
        // Arrange
        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.getPlayers();

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while retrieving the players.", errorMessage);
    }

    @Test
    public void testGetPlayers_AppliesCorrectTimeFilter() {
        // Arrange
        ArgumentCaptor<Bson> filterCaptor = ArgumentCaptor.forClass(Bson.class);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        resource.getPlayers();

        // Assert
        verify(mockCollection).find(filterCaptor.capture());
        Bson capturedFilter = filterCaptor.getValue();
        assertNotNull("Filter should not be null", capturedFilter);
        // Filter should be checking lastOnline >= (currentTime - 60)
        assertTrue("Filter should be a gte filter", capturedFilter.toString().contains("lastOnline"));
    }

    @Test
    public void testGetPlayers_SortsByPlayerName() {
        // Arrange
        ArgumentCaptor<Bson> sortCaptor = ArgumentCaptor.forClass(Bson.class);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        resource.getPlayers();

        // Assert
        verify(mockFindIterable).sort(sortCaptor.capture());
        Bson capturedSort = sortCaptor.getValue();
        assertNotNull("Sort should not be null", capturedSort);
        assertTrue("Sort should be by playerName", capturedSort.toString().contains("playerName"));
    }

    @Test
    public void testGetPlayers_VerifiesMongoServiceInteraction() {
        // Arrange
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        resource.getPlayers();

        // Assert
        verify(mockMongoService, times(1)).getPlayersCollection();
        verifyNoMoreInteractions(mockMongoService);
    }

    @Test
    public void testGetPlayer_Success() {
        // Arrange
        Integer playerId = 123;
        Document playerDoc = new Document("playerId", playerId)
                .append("playerName", "TestPlayer")
                .append("mainJob", "WAR")
                .append("mainJobLevel", 99);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(playerDoc);

        // Act
        Response response = resource.getPlayer(playerId);

        // Assert
        assertEquals(200, response.getStatus());
        Player player = (Player) response.getEntity();
        assertNotNull(player);
        assertEquals("TestPlayer", player.getPlayerName());
        assertEquals("WAR", player.getMainJob());
        verify(mockCollection).find(any(Bson.class));
    }

    @Test
    public void testGetPlayer_NotFound() {
        // Arrange
        Integer playerId = 999;

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.getPlayer(playerId);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
    }

    @Test
    public void testGetPlayer_NullPlayerId() {
        // Arrange
        Integer playerId = null;

        // Act
        Response response = resource.getPlayer(playerId);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId query parameter is required", errorMessage);
        verify(mockCollection, never()).find(any(Bson.class));
    }

    @Test
    public void testGetPlayer_DatabaseError() {
        // Arrange
        Integer playerId = 123;

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.getPlayer(playerId);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while retrieving the player.", errorMessage);
    }
  }