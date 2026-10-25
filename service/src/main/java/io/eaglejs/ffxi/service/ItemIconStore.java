package io.eaglejs.ffxi.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Permanent, write-once store of item icons: one {@code <itemId>.png} per item.
 * Existing icons are never overwritten.
 */
public class ItemIconStore {

    public static final int MAX_ITEM_ID = 0xFFFF;
    public static final int MAX_ICON_BYTES = 64 * 1024;
    public static final int ICON_SIZE = 32;

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final Path dir;

    public ItemIconStore(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create item icon directory: " + dir, e);
        }
    }

    public static boolean isValidItemId(int id) {
        return id >= 0 && id <= MAX_ITEM_ID;
    }

    /** Returns the icon bytes, or null when no icon is stored. */
    public byte[] read(int itemId) throws IOException {
        Path file = fileFor(itemId);
        return Files.isRegularFile(file) ? Files.readAllBytes(file) : null;
    }

    public List<Integer> missing(List<Integer> itemIds) {
        List<Integer> missing = new ArrayList<>();
        for (Integer id : itemIds) {
            if (!missing.contains(id) && !Files.isRegularFile(fileFor(id))) {
                missing.add(id);
            }
        }
        return missing;
    }

    /**
     * Stores the icon if none exists yet.
     *
     * @return true when the icon was written, false when one already existed
     */
    public boolean storeIfAbsent(int itemId, byte[] png) throws IOException {
        Path target = fileFor(itemId);
        if (Files.exists(target)) {
            return false;
        }
        Path temp = Files.createTempFile(dir, itemId + "-", ".tmp");
        try {
            Files.write(temp, png);
            // Without REPLACE_EXISTING the move fails if a concurrent upload won the race.
            Files.move(temp, target);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /** Returns an error description when the bytes are not a 32x32 PNG, otherwise null. */
    public static String validatePng(byte[] data) {
        if (data == null || data.length == 0) {
            return "icon body is empty";
        }
        if (data.length > MAX_ICON_BYTES) {
            return "icon exceeds " + MAX_ICON_BYTES + " bytes";
        }
        // signature (8) + IHDR length (4) + 'IHDR' (4) + width (4) + height (4)
        if (data.length < 24) {
            return "not a PNG";
        }
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (data[i] != PNG_SIGNATURE[i]) {
                return "not a PNG";
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if (buffer.getInt(8) != 13 || buffer.getInt(12) != 0x49484452) {
            return "not a PNG (missing IHDR)";
        }
        int width = buffer.getInt(16);
        int height = buffer.getInt(20);
        if (width != ICON_SIZE || height != ICON_SIZE) {
            return "icon must be " + ICON_SIZE + "x" + ICON_SIZE + " (got " + width + "x" + height + ")";
        }
        return null;
    }

    private Path fileFor(int itemId) {
        if (!isValidItemId(itemId)) {
            throw new IllegalArgumentException("itemId out of range: " + itemId);
        }
        return dir.resolve(itemId + ".png");
    }
}
