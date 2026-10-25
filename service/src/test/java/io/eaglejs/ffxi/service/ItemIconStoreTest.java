package io.eaglejs.ffxi.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ItemIconStoreTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private ItemIconStore store;
    private Path dir;

    public static byte[] png(int width, int height) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", out);
        return out.toByteArray();
    }

    @Before
    public void setUp() throws Exception {
        dir = tmp.getRoot().toPath().resolve("icons");
        store = new ItemIconStore(dir);
    }

    @Test
    public void createsDirectory() {
        assertTrue(Files.isDirectory(dir));
    }

    @Test
    public void readReturnsNullWhenAbsent() throws Exception {
        assertNull(store.read(4096));
    }

    @Test
    public void storesThenReadsBack() throws Exception {
        byte[] icon = png(32, 32);
        assertTrue(store.storeIfAbsent(4096, icon));
        assertArrayEquals(icon, store.read(4096));
    }

    @Test
    public void neverOverwritesAnExistingIcon() throws Exception {
        byte[] first = png(32, 32);
        byte[] second = new byte[first.length];
        assertTrue(store.storeIfAbsent(7, first));
        assertFalse(store.storeIfAbsent(7, second));
        assertArrayEquals(first, store.read(7));
    }

    @Test
    public void leavesNoTempFilesBehind() throws Exception {
        store.storeIfAbsent(1, png(32, 32));
        store.storeIfAbsent(1, png(32, 32));
        try (var files = Files.list(dir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    public void missingReturnsOnlyUnstoredAndDeduplicates() throws Exception {
        store.storeIfAbsent(2, png(32, 32));
        assertEquals(List.of(1, 3), store.missing(List.of(1, 2, 3, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfRangeIds() throws Exception {
        store.read(70000);
    }

    @Test
    public void validatesPng() throws Exception {
        assertNull(ItemIconStore.validatePng(png(32, 32)));
        assertNotNull(ItemIconStore.validatePng(png(16, 16)));
        assertNotNull(ItemIconStore.validatePng(new byte[0]));
        assertNotNull(ItemIconStore.validatePng("GIF89a-not-a-png-at-all-xxxxxxxxxxxx".getBytes()));
        assertNotNull(ItemIconStore.validatePng(new byte[ItemIconStore.MAX_ICON_BYTES + 1]));
    }
}
