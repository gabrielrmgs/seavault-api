package br.dev.irontech.seavault.files.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileStorageTest {

    private LocalFileStorage storageRootedAt(Path dir) {
        LocalFileStorage storage = new LocalFileStorage();
        storage.baseDir = dir.toString();
        return storage;
    }

    @Test
    void storeThenReadReturnsSameBytes(@TempDir Path dir) {
        LocalFileStorage storage = storageRootedAt(dir);
        byte[] content = "conteudo-de-teste".getBytes(StandardCharsets.UTF_8);

        storage.store("user-1/abc-123", content);

        assertArrayEquals(content, storage.read("user-1/abc-123"));
    }

    @Test
    void deleteRemovesStoredObject(@TempDir Path dir) {
        LocalFileStorage storage = storageRootedAt(dir);
        storage.store("user-1/to-delete", new byte[]{1, 2, 3});

        storage.delete("user-1/to-delete");

        assertThrows(RuntimeException.class, () -> storage.read("user-1/to-delete"));
    }

    @Test
    void deleteOfMissingKeyIsNoOp(@TempDir Path dir) {
        LocalFileStorage storage = storageRootedAt(dir);
        storage.delete("user-1/never-existed");
    }

    @Test
    void rejectsStorageKeyOutsideBaseDir(@TempDir Path dir) {
        LocalFileStorage storage = storageRootedAt(dir);
        String outsideName = "outside-storage-object-" + dir.getFileName();
        Path outside = dir.getParent().resolve(outsideName);
        assertFalse(Files.exists(outside));

        assertThrows(IllegalArgumentException.class,
                () -> storage.store("../" + outsideName, new byte[]{9}));

        assertFalse(Files.exists(outside));
    }
}
