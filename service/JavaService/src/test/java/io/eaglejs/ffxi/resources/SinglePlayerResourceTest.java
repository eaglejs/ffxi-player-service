package io.eaglejs.ffxi.resources;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.models.SetBuffsRequest;
import io.eaglejs.ffxi.models.SetCapacityPointsRequest;
import io.eaglejs.ffxi.models.SetGilRequest;
import io.eaglejs.ffxi.models.SetHppRequest;
import io.eaglejs.ffxi.models.SetJobsRequest;
import io.eaglejs.ffxi.models.SetMeritsRequest;
import io.eaglejs.ffxi.models.SetMppRequest;
import io.eaglejs.ffxi.models.SetOnlineRequest;
import io.eaglejs.ffxi.models.SetStatusRequest;
import io.eaglejs.ffxi.models.SetTpRequest;
import io.eaglejs.ffxi.models.SetZoneRequest;
import io.eaglejs.ffxi.service.MongoDBService;
import io.eaglejs.ffxi.websocket.PlayerWebSocket;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.util.List;

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
        player.setGil(10000);

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

    @Test
    public void testSetGil_Success() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setGil(50000L);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Gil: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetGil_FormatsPlayerNameToLowercase() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setGil(100000L);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setGil(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetGil_PlayerNotFound() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setGil(50000L);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetGil_NullRequest() {
        // Arrange
        SetGilRequest request = null;

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and gil are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetGil_MissingPlayerId() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerName("TestPlayer");
        request.setGil(50000L);

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and gil are required", response.getEntity());
    }

    @Test
    public void testSetGil_MissingPlayerName() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(123);
        request.setGil(50000L);

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and gil are required", response.getEntity());
    }

    @Test
    public void testSetGil_MissingGil() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and gil are required", response.getEntity());
    }

    @Test
    public void testSetGil_DatabaseError() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setGil(50000L);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player gil.", errorMessage);
    }

    @Test
    public void testSetGil_UpdateFailure() {
        // Arrange
        SetGilRequest request = new SetGilRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setGil(50000L);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setGil(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player gil", errorMessage);
    }

    @Test
    public void testSetStatus_Success() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setStatus(1);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Status: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetStatus_FormatsPlayerNameToLowercase() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setStatus(2);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setStatus(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetStatus_PlayerNotFound() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setStatus(1);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetStatus_NullRequest() {
        // Arrange
        SetStatusRequest request = null;

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and status are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetStatus_MissingPlayerId() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerName("TestPlayer");
        request.setStatus(1);

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and status are required", response.getEntity());
    }

    @Test
    public void testSetStatus_MissingPlayerName() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(123);
        request.setStatus(1);

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and status are required", response.getEntity());
    }

    @Test
    public void testSetStatus_MissingStatus() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and status are required", response.getEntity());
    }

    @Test
    public void testSetStatus_DatabaseError() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setStatus(1);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player status.", errorMessage);
    }

    @Test
    public void testSetStatus_UpdateFailure() {
        // Arrange
        SetStatusRequest request = new SetStatusRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setStatus(1);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setStatus(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player status", errorMessage);
    }

    @Test
    public void testSetHpp_Success() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setHpp(75);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("HP: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetHpp_FormatsPlayerNameToLowercase() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setHpp(100);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setHpp(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetHpp_PlayerNotFound() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setHpp(50);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetHpp_NullRequest() {
        // Arrange
        SetHppRequest request = null;

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and hpp are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetHpp_MissingPlayerId() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerName("TestPlayer");
        request.setHpp(75);

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and hpp are required", response.getEntity());
    }

    @Test
    public void testSetHpp_MissingPlayerName() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(123);
        request.setHpp(75);

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and hpp are required", response.getEntity());
    }

    @Test
    public void testSetHpp_MissingHpp() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and hpp are required", response.getEntity());
    }

    @Test
    public void testSetHpp_DatabaseError() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setHpp(75);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player hpp.", errorMessage);
    }

    @Test
    public void testSetHpp_UpdateFailure() {
        // Arrange
        SetHppRequest request = new SetHppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setHpp(75);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setHpp(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player hpp", errorMessage);
    }

    @Test
    public void testSetMpp_Success() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setMpp(50);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("MP: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetMpp_FormatsPlayerNameToLowercase() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setMpp(100);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setMpp(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetMpp_PlayerNotFound() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setMpp(25);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetMpp_NullRequest() {
        // Arrange
        SetMppRequest request = null;

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and mpp are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetMpp_MissingPlayerId() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerName("TestPlayer");
        request.setMpp(50);

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and mpp are required", response.getEntity());
    }

    @Test
    public void testSetMpp_MissingPlayerName() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(123);
        request.setMpp(50);

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and mpp are required", response.getEntity());
    }

    @Test
    public void testSetMpp_MissingMpp() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and mpp are required", response.getEntity());
    }

    @Test
    public void testSetMpp_DatabaseError() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setMpp(50);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player mpp.", errorMessage);
    }

    @Test
    public void testSetMpp_UpdateFailure() {
        // Arrange
        SetMppRequest request = new SetMppRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setMpp(50);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setMpp(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player mpp", errorMessage);
    }

    @Test
    public void testSetTp_Success() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTp(1000);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("TP: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetTp_FormatsPlayerNameToLowercase() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setTp(2500);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setTp(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetTp_PlayerNotFound() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setTp(500);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetTp_NullRequest() {
        // Arrange
        SetTpRequest request = null;

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and tp are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetTp_MissingPlayerId() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerName("TestPlayer");
        request.setTp(1000);

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and tp are required", response.getEntity());
    }

    @Test
    public void testSetTp_MissingPlayerName() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(123);
        request.setTp(1000);

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and tp are required", response.getEntity());
    }

    @Test
    public void testSetTp_MissingTp() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and tp are required", response.getEntity());
    }

    @Test
    public void testSetTp_DatabaseError() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTp(1000);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player tp.", errorMessage);
    }

    @Test
    public void testSetTp_UpdateFailure() {
        // Arrange
        SetTpRequest request = new SetTpRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTp(1000);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setTp(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player tp", errorMessage);
    }

    @Test
    public void testSetZone_Success() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setZone("Bastok Markets");

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Zone: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetZone_FormatsPlayerNameToLowercase() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setZone("Jeuno");

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setZone(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetZone_PlayerNotFound() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setZone("Windurst");

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetZone_NullRequest() {
        // Arrange
        SetZoneRequest request = null;

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and zone are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetZone_MissingPlayerId() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerName("TestPlayer");
        request.setZone("San d'Oria");

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and zone are required", response.getEntity());
    }

    @Test
    public void testSetZone_MissingPlayerName() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(123);
        request.setZone("San d'Oria");

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and zone are required", response.getEntity());
    }

    @Test
    public void testSetZone_MissingZone() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and zone are required", response.getEntity());
    }

    @Test
    public void testSetZone_DatabaseError() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setZone("Bastok Markets");

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player zone.", errorMessage);
    }

    @Test
    public void testSetZone_UpdateFailure() {
        // Arrange
        SetZoneRequest request = new SetZoneRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setZone("Bastok Markets");

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setZone(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player zone", errorMessage);
    }

    @Test
    public void testSetMerits_Success() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(50);
        request.setMax(75);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Merits: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetMerits_FormatsPlayerNameToLowercase() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setTotal(30);
        request.setMax(60);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setMerits(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetMerits_PlayerNotFound() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setTotal(10);
        request.setMax(20);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetMerits_NullRequest() {
        // Arrange
        SetMeritsRequest request = null;

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, total, and max are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetMerits_MissingPlayerId() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerName("TestPlayer");
        request.setTotal(25);
        request.setMax(50);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, total, and max are required", response.getEntity());
    }

    @Test
    public void testSetMerits_MissingPlayerName() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(123);
        request.setTotal(25);
        request.setMax(50);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, total, and max are required", response.getEntity());
    }

    @Test
    public void testSetMerits_MissingTotal() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setMax(50);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, total, and max are required", response.getEntity());
    }

    @Test
    public void testSetMerits_MissingMax() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(25);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, total, and max are required", response.getEntity());
    }

    @Test
    public void testSetMerits_DatabaseError() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(40);
        request.setMax(80);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player merits.", errorMessage);
    }

    @Test
    public void testSetMerits_UpdateFailure() {
        // Arrange
        SetMeritsRequest request = new SetMeritsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(35);
        request.setMax(70);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setMerits(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player merits", errorMessage);
    }

    @Test
    public void testSetCapacityPoints_Success() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(5000);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Capacity points: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetCapacityPoints_FormatsPlayerNameToLowercase() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setTotal(3000);

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setCapacityPoints(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetCapacityPoints_PlayerNotFound() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setTotal(1000);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetCapacityPoints_NullRequest() {
        // Arrange
        SetCapacityPointsRequest request = null;

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and total are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetCapacityPoints_MissingPlayerId() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerName("TestPlayer");
        request.setTotal(2500);

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and total are required", response.getEntity());
    }

    @Test
    public void testSetCapacityPoints_MissingPlayerName() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(123);
        request.setTotal(2500);

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and total are required", response.getEntity());
    }

    @Test
    public void testSetCapacityPoints_MissingTotal() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and total are required", response.getEntity());
    }

    @Test
    public void testSetCapacityPoints_DatabaseError() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(4000);

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player capacity points.", errorMessage);
    }

    @Test
    public void testSetCapacityPoints_UpdateFailure() {
        // Arrange
        SetCapacityPointsRequest request = new SetCapacityPointsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setTotal(3500);

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setCapacityPoints(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player capacity points", errorMessage);
    }

    @Test
    public void testGetBuffs_Success() {
        // Arrange
        Integer playerId = 123;
        List<Integer> buffs = java.util.Arrays.asList(1, 2, 3, 4, 5);
        Document existingPlayer = new Document("playerId", playerId).append("buffs", buffs);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);

        // Act
        Response response = resource.getBuffs(playerId);

        // Assert
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<Integer> returnedBuffs = (List<Integer>) response.getEntity();
        assertNotNull(returnedBuffs);
        assertEquals(5, returnedBuffs.size());
        assertEquals(buffs, returnedBuffs);
        verify(mockCollection).find(any(Bson.class));
    }

    @Test
    public void testGetBuffs_EmptyBuffsList() {
        // Arrange
        Integer playerId = 456;
        List<Integer> buffs = new java.util.ArrayList<>();
        Document existingPlayer = new Document("playerId", playerId).append("buffs", buffs);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);

        // Act
        Response response = resource.getBuffs(playerId);

        // Assert
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<Integer> returnedBuffs = (List<Integer>) response.getEntity();
        assertNotNull(returnedBuffs);
        assertEquals(0, returnedBuffs.size());
    }

    @Test
    public void testGetBuffs_NoBuffsField() {
        // Arrange
        Integer playerId = 789;
        Document existingPlayer = new Document("playerId", playerId);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);

        // Act
        Response response = resource.getBuffs(playerId);

        // Assert
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<Integer> returnedBuffs = (List<Integer>) response.getEntity();
        assertNotNull(returnedBuffs);
        assertEquals(0, returnedBuffs.size());
    }

    @Test
    public void testGetBuffs_PlayerNotFound() {
        // Arrange
        Integer playerId = 999;
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.getBuffs(playerId);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
    }

    @Test
    public void testGetBuffs_NullPlayerId() {
        // Arrange
        Integer playerId = null;

        // Act
        Response response = resource.getBuffs(playerId);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId query parameter is required", errorMessage);
        verify(mockCollection, never()).find(any(Bson.class));
    }

    @Test
    public void testGetBuffs_DatabaseError() {
        // Arrange
        Integer playerId = 123;
        
        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.getBuffs(playerId);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while retrieving player buffs.", errorMessage);
    }

    @Test
    public void testSetBuffs_Success() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setBuffs(java.util.Arrays.asList(1, 2, 3, 4, 5));

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Buffs: OK", response.getEntity());
        verify(mockCollection).find(any(Bson.class));
        verify(mockCollection).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetBuffs_FormatsPlayerNameToLowercase() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(456);
        request.setPlayerName("TestPlayer");
        request.setBuffs(java.util.Arrays.asList(10, 20, 30));

        Document existingPlayer = new Document("playerId", 456);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), updateCaptor.capture())).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        resource.setBuffs(request);

        // Assert
        Bson capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
        assertTrue(capturedUpdate.toString().contains("testplayer"));
    }

    @Test
    public void testSetBuffs_EmptyBuffsList() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(789);
        request.setPlayerName("TestPlayer");
        request.setBuffs(new java.util.ArrayList<>());

        Document existingPlayer = new Document("playerId", 789);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(1L);

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Buffs: OK", response.getEntity());
    }

    @Test
    public void testSetBuffs_PlayerNotFound() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(999);
        request.setPlayerName("NonExistent");
        request.setBuffs(java.util.Arrays.asList(1, 2, 3));

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(404, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertTrue(errorMessage.contains("Player not found"));
        assertTrue(errorMessage.contains("999"));
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetBuffs_NullRequest() {
        // Arrange
        SetBuffsRequest request = null;

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(400, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("playerId, playerName, and buffs are required", errorMessage);
        verify(mockCollection, never()).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testSetBuffs_MissingPlayerId() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerName("TestPlayer");
        request.setBuffs(java.util.Arrays.asList(1, 2, 3));

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and buffs are required", response.getEntity());
    }

    @Test
    public void testSetBuffs_MissingPlayerName() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(123);
        request.setBuffs(java.util.Arrays.asList(1, 2, 3));

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and buffs are required", response.getEntity());
    }

    @Test
    public void testSetBuffs_MissingBuffs() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(400, response.getStatus());
        assertEquals("playerId, playerName, and buffs are required", response.getEntity());
    }

    @Test
    public void testSetBuffs_DatabaseError() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setBuffs(java.util.Arrays.asList(5, 10, 15));

        when(mockCollection.find(any(Bson.class))).thenThrow(new RuntimeException("Database connection lost"));

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("An error occurred while updating player buffs.", errorMessage);
    }

    @Test
    public void testSetBuffs_UpdateFailure() {
        // Arrange
        SetBuffsRequest request = new SetBuffsRequest();
        request.setPlayerId(123);
        request.setPlayerName("TestPlayer");
        request.setBuffs(java.util.Arrays.asList(7, 14, 21));

        Document existingPlayer = new Document("playerId", 123);
        
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(existingPlayer);
        when(mockCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getModifiedCount()).thenReturn(0L);
        when(mockUpdateResult.getMatchedCount()).thenReturn(0L);

        // Act
        Response response = resource.setBuffs(request);

        // Assert
        assertEquals(500, response.getStatus());
        String errorMessage = (String) response.getEntity();
        assertEquals("Failed to update player buffs", errorMessage);
    }
}
