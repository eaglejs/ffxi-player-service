package io.eaglejs.ffxi.resources;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import io.eaglejs.ffxi.models.Player;
import io.eaglejs.ffxi.service.MongoDBService;
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

public class PlayerResourceTest {

    private MongoDBService mockMongoService;
    private MongoCollection<Document> mockCollection;
    private FindIterable<Document> mockFindIterable;
    private PlayerResource resource;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockMongoService = mock(MongoDBService.class);
        mockCollection = (MongoCollection<Document>) mock(MongoCollection.class);
        mockFindIterable = (FindIterable<Document>) mock(FindIterable.class);
        resource = new PlayerResource(mockMongoService);

        when(mockMongoService.getPlayersCollection()).thenReturn(mockCollection);
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
}
