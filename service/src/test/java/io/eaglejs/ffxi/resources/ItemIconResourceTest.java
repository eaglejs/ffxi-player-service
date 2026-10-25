package io.eaglejs.ffxi.resources;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.eaglejs.ffxi.service.ItemIconStore;
import io.eaglejs.ffxi.service.ItemIconStoreTest;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("unchecked")
public class ItemIconResourceTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private ItemIconResource resource;

    @Before
    public void setUp() {
        resource = new ItemIconResource(new ItemIconStore(tmp.getRoot().toPath().resolve("icons")));
    }

    private Response put(String id, byte[] body) {
        return resource.putIcon(id, new ByteArrayInputStream(body));
    }

    @Test
    public void getReturns404WhenMissing() {
        assertEquals(404, resource.getIcon("4096").getStatus());
    }

    @Test
    public void uploadThenGetServesImmutableCachedPng() throws Exception {
        byte[] icon = ItemIconStoreTest.png(32, 32);
        Response created = put("4096", icon);
        assertEquals(201, created.getStatus());

        Response response = resource.getIcon("4096");
        assertEquals(200, response.getStatus());
        assertArrayEquals(icon, (byte[]) response.getEntity());
        assertEquals("image/png", response.getMediaType().toString());
        String cacheControl = response.getHeaderString("Cache-Control");
        assertTrue(cacheControl, cacheControl.contains("max-age=31536000"));
        assertTrue(cacheControl, cacheControl.contains("immutable"));
    }

    @Test
    public void secondUploadIsNoOpAndKeepsFirst() throws Exception {
        byte[] first = ItemIconStoreTest.png(32, 32);
        put("5", first);
        Response second = put("5", ItemIconStoreTest.png(32, 32));
        assertEquals(200, second.getStatus());
        assertEquals("Icon: EXISTS", second.getEntity());
        assertArrayEquals(first, (byte[]) resource.getIcon("5").getEntity());
    }

    @Test
    public void rejectsBadUploads() throws Exception {
        assertEquals(400, put("1", ItemIconStoreTest.png(16, 16)).getStatus());
        assertEquals(400, put("1", "not a png".getBytes()).getStatus());
        assertEquals(400, put("1", new byte[0]).getStatus());
        assertEquals(400, put("70000", ItemIconStoreTest.png(32, 32)).getStatus());
        assertEquals(413, put("1", new byte[ItemIconStore.MAX_ICON_BYTES + 100]).getStatus());
    }

    @Test
    public void outOfRangeIdOnGetIs404() {
        assertEquals(404, resource.getIcon("70000").getStatus());
    }

    @Test
    public void missingReportsOnlyUnstoredIds() throws Exception {
        put("2", ItemIconStoreTest.png(32, 32));
        Response response = resource.missing(Map.of("ids", List.of(1, 2, 3)));
        assertEquals(200, response.getStatus());
        assertEquals(List.of(1, 3), ((Map<String, Object>) response.getEntity()).get("missing"));
    }

    @Test
    public void missingValidatesInput() {
        assertEquals(400, resource.missing(null).getStatus());
        assertEquals(400, resource.missing(Map.of()).getStatus());
        assertEquals(400, resource.missing(Map.of("ids", List.of(-1))).getStatus());
        assertEquals(400, resource.missing(Map.of("ids", List.of(70000))).getStatus());
        List<Integer> tooMany = java.util.stream.IntStream.range(0, ItemIconResource.MAX_MISSING_IDS + 1)
                .boxed().toList();
        assertEquals(400, resource.missing(Map.of("ids", tooMany)).getStatus());
    }
}
