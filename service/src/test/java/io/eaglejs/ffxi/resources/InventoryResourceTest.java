package io.eaglejs.ffxi.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.eaglejs.ffxi.dao.InventoryDao;
import io.eaglejs.ffxi.dao.InventoryDao.SaveResult;
import io.eaglejs.ffxi.dao.InventoryDao.SearchCriteria;
import io.eaglejs.ffxi.dao.InventoryDao.SearchPage;
import io.eaglejs.ffxi.models.InventoryItem;
import io.eaglejs.ffxi.models.InventorySnapshotRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class InventoryResourceTest {

    private InventoryDao dao;
    private InventoryResource resource;

    @Before
    public void setUp() {
        dao = mock(InventoryDao.class);
        resource = new InventoryResource(dao);
    }

    private static InventorySnapshotRequest validRequest() {
        InventoryItem item = new InventoryItem();
        item.setId(1);
        item.setCount(2);
        InventorySnapshotRequest request = new InventorySnapshotRequest();
        request.setPlayerId(7);
        request.setPlayerName("Sungjinwoo");
        request.setContentHash("hash");
        request.setBags(Map.of("inventory", List.of(item)));
        return request;
    }

    @Test
    public void setInventoryReturnsOkWhenSaved() {
        when(dao.save(any())).thenReturn(SaveResult.SAVED);
        Response response = resource.setInventory(validRequest());
        assertEquals(200, response.getStatus());
        assertEquals("Inventory: OK", response.getEntity());
    }

    @Test
    public void setInventoryReturnsOkWhenUnchanged() {
        when(dao.save(any())).thenReturn(SaveResult.UNCHANGED);
        Response response = resource.setInventory(validRequest());
        assertEquals(200, response.getStatus());
        assertEquals("Inventory: OK", response.getEntity());
    }

    @Test
    public void setInventoryReturnsStaleBody() {
        when(dao.save(any())).thenReturn(SaveResult.STALE);
        Response response = resource.setInventory(validRequest());
        assertEquals(200, response.getStatus());
        assertEquals("Inventory: STALE", response.getEntity());
    }

    @Test
    public void setInventoryRejectsInvalidBodyListingFields() {
        InventorySnapshotRequest request = new InventorySnapshotRequest();
        Response response = resource.setInventory(request);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertTrue(message.contains("playerId is required"));
        assertTrue(message.contains("contentHash is required"));
        verify(dao, never()).save(any());
    }

    @Test
    public void setInventoryRejectsNullBody() {
        assertEquals(400, resource.setInventory(null).getStatus());
    }

    @Test
    public void setInventoryReturns500OnDaoFailure() {
        when(dao.save(any())).thenThrow(new RuntimeException("mongo down"));
        Response response = resource.setInventory(validRequest());
        assertEquals(500, response.getStatus());
        assertEquals("An error occurred while updating player inventory.", response.getEntity());
    }

    @Test
    public void getInventoryReturns404WhenMissing() {
        when(dao.findByPlayerId(7)).thenReturn(null);
        assertEquals(404, resource.getInventory(7).getStatus());
    }

    @Test
    public void getInventoryStripsInternalFieldsAndFormatsDates() {
        Document stored = new Document("_id", "x").append("playerId", 7).append("playerNameLower", "a")
                .append("updatedAt", new Date(0));
        when(dao.findByPlayerId(7)).thenReturn(stored);

        Response response = resource.getInventory(7);

        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertEquals(200, response.getStatus());
        assertTrue(!body.containsKey("_id"));
        assertTrue(!body.containsKey("playerNameLower"));
        assertEquals("1970-01-01T00:00:00Z", body.get("updatedAt"));
    }

    @Test
    public void searchRequiresItem() {
        assertEquals(400, resource.searchInventories(null, null, null, null, false, null, null).getStatus());
    }

    @Test
    public void searchRejectsNameWithExplanation() {
        Response response = resource.searchInventories(null, "Cure", null, null, false, null, null);
        assertEquals(400, response.getStatus());
        assertTrue(((String) response.getEntity()).contains("client-side"));
    }

    @Test
    public void searchRejectsBadItemsAndLimits() {
        assertEquals(400, resource.searchInventories(List.of("abc"), null, null, null, false, null, null).getStatus());
        assertEquals(400, resource.searchInventories(List.of("-1"), null, null, null, false, null, null).getStatus());
        assertEquals(400, resource.searchInventories(List.of("1"), null, null, null, false, 0, null).getStatus());
        assertEquals(400, resource.searchInventories(List.of("1"), null, null, null, false, 501, null).getStatus());
        assertEquals(400, resource.searchInventories(List.of("1"), null, null, null, false, null, -1).getStatus());
    }

    @Test
    public void searchReturns404ForUnknownPlayer() {
        when(dao.playerExists(any())).thenReturn(false);
        Response response = resource.searchInventories(List.of("1"), null, "ghost", null, false, null, null);
        assertEquals(404, response.getStatus());
        verify(dao, never()).search(any());
    }

    @Test
    public void searchParsesCommaSeparatedAndRepeatedItemsAndNumericPlayer() {
        when(dao.playerExists(any())).thenReturn(true);
        when(dao.search(any())).thenReturn(new SearchPage(0, new ArrayList<>()));

        Response response = resource.searchInventories(List.of("1,2", "3", "2"), null, "42", List.of("satchel"), true, 5, 10);

        assertEquals(200, response.getStatus());
        ArgumentCaptor<SearchCriteria> captor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(dao).search(captor.capture());
        SearchCriteria criteria = captor.getValue();
        assertEquals(List.of(1, 2, 3), criteria.itemIds);
        assertEquals(Integer.valueOf(42), criteria.playerId);
        assertNull(criteria.playerNameLower);
        assertEquals(List.of("satchel"), criteria.bags);
        assertTrue(criteria.includeKeyItems);
        assertEquals(5, criteria.limit);
        assertEquals(10, criteria.offset);

        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertEquals(0L, body.get("totalMatches"));
        assertTrue(body.containsKey("query"));
    }

    @Test
    public void searchTreatsNonNumericPlayerAsLowercasedName() {
        when(dao.playerExists(any())).thenReturn(true);
        when(dao.search(any())).thenReturn(new SearchPage(0, new ArrayList<>()));

        resource.searchInventories(List.of("1"), null, "Sungjinwoo", null, false, null, null);

        ArgumentCaptor<SearchCriteria> captor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(dao).search(captor.capture());
        assertEquals("sungjinwoo", captor.getValue().playerNameLower);
        assertNull(captor.getValue().playerId);
    }

    @Test
    public void playerAliasDelegatesWithPlayerFilter() {
        when(dao.playerExists(any())).thenReturn(true);
        when(dao.search(any())).thenReturn(new SearchPage(0, new ArrayList<>()));

        resource.searchPlayerInventory(9, List.of("5"), null, null, false, null, null);

        ArgumentCaptor<SearchCriteria> captor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(dao).search(captor.capture());
        assertEquals(Integer.valueOf(9), captor.getValue().playerId);
    }

    @Test
    public void sizeFilterRejectsOversizedInventoryPost() throws Exception {
        RequestSizeLimitFilter filter = new RequestSizeLimitFilter();
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(context.getMethod()).thenReturn("POST");
        when(context.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("player/set_inventory");
        when(context.getLength()).thenReturn((int) RequestSizeLimitFilter.MAX_INVENTORY_BODY_BYTES + 1);

        filter.filter(context);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(context).abortWith(captor.capture());
        assertEquals(413, captor.getValue().getStatus());
    }

    @Test
    public void sizeFilterIgnoresNormalBodiesAndOtherPaths() throws Exception {
        RequestSizeLimitFilter filter = new RequestSizeLimitFilter();
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(context.getMethod()).thenReturn("POST");
        when(context.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("player/set_inventory");
        when(context.getLength()).thenReturn(35_000);

        filter.filter(context);
        verify(context, never()).abortWith(any());
    }
}
