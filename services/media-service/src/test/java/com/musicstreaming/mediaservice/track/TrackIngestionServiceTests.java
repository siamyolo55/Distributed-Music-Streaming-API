package com.musicstreaming.mediaservice.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.musicstreaming.common.events.EventEnvelope;
import com.musicstreaming.common.events.TrackUploadedEvent;
import com.musicstreaming.mediaservice.storage.MediaObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TrackIngestionServiceTests {

    @Mock
    private MediaObjectStorage storage;

    private TrackIngestionService service;

    @BeforeEach
    void setUp() {
        service = new TrackIngestionService(storage);
    }

    @Test
    void ingestStoresAudioAndBuildsEventEnvelope() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[] {1, 2, 3});
        when(storage.store("artist-1", file))
                .thenReturn(new MediaObjectStorage.StoredObject("raw/artist-1/id.mp3", "/local-media/raw/artist-1/id.mp3", "http://localhost:8082/local-media/raw/artist-1/id.mp3"));

        EventEnvelope<TrackUploadedEvent> envelope = service.ingest("My Song", "artist-1", file);

        assertThat(envelope.eventType()).isEqualTo("TrackUploaded");
        assertThat(envelope.payload().artistId()).isEqualTo("artist-1");
        assertThat(envelope.payload().title()).isEqualTo("My Song");
        assertThat(envelope.payload().storagePath()).isEqualTo("/local-media/raw/artist-1/id.mp3");
    }

    @Test
    void ingestRejectsNonAudioFile() {
        MockMultipartFile file = new MockMultipartFile("file", "readme.txt", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> service.ingest("Bad", "artist-1", file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only audio files are supported");
    }
}
