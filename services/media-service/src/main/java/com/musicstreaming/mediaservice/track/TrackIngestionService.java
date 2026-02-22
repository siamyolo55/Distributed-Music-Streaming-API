package com.musicstreaming.mediaservice.track;

import com.musicstreaming.common.events.EventEnvelope;
import com.musicstreaming.common.events.TrackUploadedEvent;
import com.musicstreaming.mediaservice.track.domain.TrackRecord;
import com.musicstreaming.mediaservice.track.domain.TrackRecordRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrackIngestionService {

    private final TrackRecordRepository trackRecordRepository;
    private final String mockFileUrl;

    public TrackIngestionService(
            TrackRecordRepository trackRecordRepository,
            @Value("${media.mock.file-url}") String mockFileUrl) {
        this.trackRecordRepository = trackRecordRepository;
        this.mockFileUrl = mockFileUrl;
    }

    @Transactional
    public EventEnvelope<TrackUploadedEvent> ingest(
            String title,
            String artistId,
            String artistName,
            String genre,
            MultipartFile file) {
        validateFile(file);

        String trackId = "trk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Instant now = Instant.now();

        trackRecordRepository.save(new TrackRecord(
                trackId,
                artistId.trim(),
                artistName.trim(),
                title.trim(),
                genre.trim(),
                mockFileUrl,
                now));

        TrackUploadedEvent event = new TrackUploadedEvent(
                trackId,
                artistId,
                title,
                mockFileUrl,
                "1.0.0"
        );

        return new EventEnvelope<>(
                "TrackUploaded",
                "1.0.0",
                now,
                event
        );
    }

    @Transactional(readOnly = true)
    public List<TrackView> listTracks() {
        return trackRecordRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(t -> new TrackView(
                        t.getTrackId(),
                        t.getArtistId(),
                        t.getArtistName(),
                        t.getTitle(),
                        t.getGenre(),
                        t.getFileUrl(),
                        t.getCreatedAt()))
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audio file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only audio files are supported");
        }
    }
}
