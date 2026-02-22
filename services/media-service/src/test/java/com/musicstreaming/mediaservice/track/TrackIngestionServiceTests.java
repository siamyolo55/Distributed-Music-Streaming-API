package com.musicstreaming.mediaservice.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musicstreaming.common.events.EventEnvelope;
import com.musicstreaming.common.events.TrackUploadedEvent;
import com.musicstreaming.mediaservice.track.domain.TrackRecord;
import com.musicstreaming.mediaservice.track.domain.TrackRecordRepository;
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
    private TrackRecordRepository trackRecordRepository;

    private TrackIngestionService service;

    @BeforeEach
    void setUp() {
        service = new TrackIngestionService(trackRecordRepository, "https://example.com/mock.mp3");
    }

    @Test
    void ingestStoresMetadataAndBuildsEventEnvelope() {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[] {1, 2, 3});
        when(trackRecordRepository.save(any(TrackRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventEnvelope<TrackUploadedEvent> envelope = service.ingest("My Song", "artist-1", "Artist 1", "Rock", file);

        assertThat(envelope.eventType()).isEqualTo("TrackUploaded");
        assertThat(envelope.payload().artistId()).isEqualTo("artist-1");
        assertThat(envelope.payload().title()).isEqualTo("My Song");
        assertThat(envelope.payload().storagePath()).isEqualTo("https://example.com/mock.mp3");
        verify(trackRecordRepository).save(any(TrackRecord.class));
    }

    @Test
    void ingestRejectsNonAudioFile() {
        MockMultipartFile file = new MockMultipartFile("file", "readme.txt", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> service.ingest("Bad", "artist-1", "Artist 1", "Rock", file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only audio files are supported");
    }
}
