package io.eaglejs.ffxi.resources;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.models.SetJobsRequest;
import io.eaglejs.ffxi.models.SetOnlineRequest;
import io.eaglejs.ffxi.service.MongoDBService;
import io.eaglejs.ffxi.websocket.PlayerWebSocket;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SinglePlayerResourceTest {

    private MongoDBService mockMongoService;
    private MongoCollection<Document> mockCollection;
    private FindIterable<Document> mockFindIterable;
    private UpdateResult mockUpdateResult;
    private SinglePlayerResource resource;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockMongoService = mock(MongoDBService.class);
        mockCollection = (MongoCollection<Document>) mock(MongoCollection.class);
        mockFindIterable = (FindIterable<Document>) mock(FindIterable.class);
        mockUpdateResult = mock(UpdateResult.class);
        resource = new SinglePlayerResource(mockMongoService);

        when(mockMongoService.getPlayersCollection()).thenReturn(mockCollection);
        
        PlayerWebSocket.reset();
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

    @Test
    public void testSetJobs_Success() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(123);
        request.setPlayerName("testplayer");
        request.setMainJob("WAR");
        request.setSubJob("NIN");

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Jobs: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetJobs_UpdatesCorrectFields() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(456);
        request.setPlayerName("testplayer");
        request.setMainJob("WHM");
        request.setSubJob("BLM");

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setJobs(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("WHM"));
        assertTrue(capturedUpdate.toString().contains("BLM"));
    }

    @Test
    public void testSetJobs_PlayerNotFound() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(999);
        request.setPlayerName("nonexistent");
        request.setMainJob("WAR");
        request.setSubJob("NIN");

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetJobs_NullRequest() {
        // Arrange
        SetJobsRequest request = null;

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, mainJob, and subJob are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetJobs_MissingPlayerId() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerName("testplayer");
        request.setMainJob("WAR");
        request.setSubJob("NIN");

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, mainJob, and subJob are required", response.getEntity());
    }

    @Test
    public void testSetJobs_MissingPlayerName() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(123);
        request.setMainJob("WAR");
        request.setSubJob("NIN");

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, mainJob, and subJob are required", response.getEntity());
    }

    @Test
    public void testSetJobs_MissingMainJob() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(123);
        request.setPlayerName("testplayer");
        request.setSubJob("NIN");

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, mainJob, and subJob are required", response.getEntity());
    }

    @Test
    public void testSetJobs_MissingSubJob() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(123);
        request.setPlayerName("testplayer");
        request.setMainJob("WAR");

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, mainJob, and subJob are required", response.getEntity());
    }

    @Test
    public void testSetJobs_DatabaseError() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(123);
        request.setPlayerName("testplayer");
        request.setMainJob("WAR");
        request.setSubJob("NIN");

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player jobs.", errorMessage);
    }

    @Test
    public void testSetJobs_UpdateFailure() {
        // Arrange
        SetJobsRequest request = new SetJobsRequest();
        request.setPlayerId(123);
        request.setPlayerName("testplayer");
        request.setMainJob("WAR");
        request.setSubJob("NIN");

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setJobs(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player jobs", errorMessage);
    }
}
