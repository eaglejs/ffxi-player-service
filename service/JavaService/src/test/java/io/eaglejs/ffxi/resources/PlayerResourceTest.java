package io.eaglejs.ffxi.resources;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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

        List<Document> expectedPlayers = Arrays.asList(player1, player2);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.into(any(List.class))).thenAnswer(invocation -> {
            List<Document> list = invocation.getArgument(0);
            list.addAll(expectedPlayers);
            return list;
        });

        // Act
        Response response = resource.getPlayers();

        // Assert
        assertEquals(200, response.getStatus());
        List<Document> actualPlayers = (List<Document>) response.getEntity();
        assertEquals(2, actualPlayers.size());
        assertEquals("Alice", actualPlayers.get(0).getString("playerName"));
        assertEquals("Bob", actualPlayers.get(1).getString("playerName"));

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
        List<Document> actualPlayers = (List<Document>) response.getEntity();
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
}
