package io.eaglejs.ffxi.resources;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.models.SetOnlineRequest;
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
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlayerResourceTest {

    private MongoDBService mockMongoService;
    private MongoCollection<Document> mockCollection;
    private FindIterable<Document> mockFindIterable;
    private UpdateResult mockUpdateResult;
    private PlayerResource resource;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockMongoService = mock(MongoDBService.class);
        mockCollection = (MongoCollection<Document>) mock(MongoCollection.class);
        mockFindIterable = (FindIterable<Document>) mock(FindIterable.class);
        mockUpdateResult = mock(UpdateResult.class);
        resource = new PlayerResource(mockMongoService);

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
    public void testInitializePlayer_Success() {
        // Arrange
        Player player = new Player();
        player.setPlayerId(123);
        player.setPlayerName("TestPlayer");
        player.setMainJob("WAR");
        player.setMainJobLevel(99);

        when(mockCollection.insertOne(any(Document.class))).thenReturn(null);

        // Act
        Response response = resource.initializePlayer(player);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Player initialized successfully", response.getEntity());
        verify(mockCollection).insertOne(any(Document.class));
        verify(mockMongoService).getPlayersCollection();
    }

    @Test
    public void testInitializePlayer_InsertsCorrectDocument() {
        // Arrange
        Player player = new Player();
        player.setPlayerId(456);
        player.setPlayerName("AnotherPlayer");
        player.setMainJob("WHM");
        player.setMainJobLevel(75);
        player.setGil(10000L);

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        when(mockCollection.insertOne(any(Document.class))).thenReturn(null);

        // Act
        resource.initializePlayer(player);

        // Assert
        verify(mockCollection).insertOne(docCaptor.capture());
        Document capturedDoc = docCaptor.getValue();
        assertNotNull("Document should not be null", capturedDoc);
        assertEquals(456, capturedDoc.getInteger("playerId").intValue());
        assertEquals("AnotherPlayer", capturedDoc.getString("playerName"));
        assertEquals("WHM", capturedDoc.getString("mainJob"));
        assertEquals(75, capturedDoc.getInteger("mainJobLevel").intValue());
    }

    @Test
    public void testInitializePlayer_Returns500OnDatabaseError() {
        // Arrange
        Player player = new Player();
        player.setPlayerId(789);
        player.setPlayerName("ErrorPlayer");

        when(mockCollection.insertOne(any(Document.class)))
                .thenThrow(new RuntimeException("Database insert failed"));

        // Act
        Response response = resource.initializePlayer(player);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while initializing the player.", errorMessage);
    }

    @Test
    public void testInitializePlayer_HandlesNullPlayer() {
        // Arrange
        Player player = null;

        // Act
        Response response = resource.initializePlayer(player);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("Player data cannot be null.", response.getEntity());
        verify(mockCollection, never()).insertOne(any(Document.class));
    }

    @Test
    public void testInitializePlayer_WithCompletePlayerData() {
        // Arrange
        Player player = new Player();
        player.setPlayerId(999);
        player.setPlayerName("CompletePlayer");
        player.setMainJob("PLD");
        player.setMainJobLevel(99);
        player.setSubJob("WAR");
        player.setSubJobLevel(49);
        player.setMasterLevel(20);
        player.setZone("Southern San d'Oria");
        player.setHpp(100);
        player.setMpp(100);
        player.setTp(0);

        when(mockCollection.insertOne(any(Document.class))).thenReturn(null);

        // Act
        Response response = resource.initializePlayer(player);

        // Assert
        assertEquals(200, response.getStatus());
        verify(mockCollection).insertOne(any(Document.class));
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

    @Test
    public void testSetOnline_Success() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setLastOnline(1234567890L);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Player Status: Online", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetOnline_FormatsPlayerNameToLowercase() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setLastOnline(9876543210L);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setOnline(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetOnline_PlayerNotFound() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setLastOnline(1234567890L);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetOnline_NullRequest() {
        // Arrange
        SetOnlineRequest request = null;

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and lastOnline are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetOnline_MissingPlayerId() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerName("TestPlayer");
        request.setLastOnline(1234567890L);

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and lastOnline are required", response.getEntity());
    }

    @Test
    public void testSetOnline_MissingPlayerName() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(123);
        request.setLastOnline(1234567890L);

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and lastOnline are required", response.getEntity());
    }

    @Test
    public void testSetOnline_MissingLastOnline() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and lastOnline are required", response.getEntity());
    }

    @Test
    public void testSetOnline_DatabaseError() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setLastOnline(1234567890L);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player status.", errorMessage);
    }

    @Test
    public void testSetOnline_UpdateFailure() {
        // Arrange
        SetOnlineRequest request = new SetOnlineRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setLastOnline(1234567890L);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setOnline(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player status", errorMessage);
    }
}
