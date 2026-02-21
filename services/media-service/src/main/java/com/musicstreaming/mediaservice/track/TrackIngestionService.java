package com.musicstreaming.mediaservice.track;

import com.musicstreaming.common.events.EventEnvelope;
import com.musicstreaming.common.events.TrackUploadedEvent;
import com.musicstreaming.mediaservice.storage.MediaObjectStorage;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrackIngestionService {

    private final MediaObjectStorage mediaObjectStorage;

    public TrackIngestionService(MediaObjectStorage mediaObjectStorage) {
        this.mediaObjectStorage = mediaObjectStorage;
    }

    public EventEnvelope<TrackUploadedEvent> ingest(String title, String artistId, MultipartFile file) {
        validateFile(file);

        MediaObjectStorage.StoredObject storedObject;
        try {
            storedObject = mediaObjectStorage.store(artistId, file);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store media file", ex);
        }

        TrackUploadedEvent event = new TrackUploadedEvent(
                "trk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                artistId,
                title,
                storedObject.storagePath(),
                "1.0.0"
        );

        return new EventEnvelope<>(
                "TrackUploaded",
                "1.0.0",
                Instant.now(),
                event
        );
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
