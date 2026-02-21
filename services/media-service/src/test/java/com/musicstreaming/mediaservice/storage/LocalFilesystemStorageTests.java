package com.musicstreaming.mediaservice.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalFilesystemStorageTests {

    @TempDir
    Path tempDir;

    @Test
    void storeWritesFileAndReturnsLocalPaths() throws Exception {
        MediaStorageProperties properties = new MediaStorageProperties();
        properties.getLocal().setRoot(tempDir.toString());
        properties.getLocal().setPublicBaseUrl("http://localhost:8082/local-media");
        LocalFilesystemStorage storage = new LocalFilesystemStorage(properties);

        MockMultipartFile file = new MockMultipartFile("file", "voice.mp3", "audio/mpeg", new byte[] {10, 20, 30});
        MediaObjectStorage.StoredObject stored = storage.store("artist_1", file);

        Path savedPath = tempDir.resolve(stored.objectKey());
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(stored.storagePath()).startsWith("/local-media/raw/artist_1/");
        assertThat(stored.publicUrl()).startsWith("http://localhost:8082/local-media/raw/artist_1/");
    }
}
