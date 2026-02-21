package com.musicstreaming.mediaservice.api;

import com.musicstreaming.common.events.EventEnvelope;
import com.musicstreaming.common.events.TrackUploadedEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media/tracks")
@Validated
public class TrackController {

    @PostMapping
    public ResponseEntity<EventEnvelope<TrackUploadedEvent>> upload(@Valid @RequestBody TrackUploadRequest request) {
        TrackUploadedEvent event = new TrackUploadedEvent(
                "trk_" + request.title().hashCode(),
                request.artistId(),
                request.title(),
                "/bucket/raw/" + request.title().replace(" ", "_") + ".mp3",
                "1.0.0"
        );

        EventEnvelope<TrackUploadedEvent> envelope = new EventEnvelope<>(
                "TrackUploaded",
                "1.0.0",
                Instant.now(),
                event
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(envelope);
    }

    public record TrackUploadRequest(
            @NotBlank(message = "must not be blank") String title,
            @NotBlank(message = "must not be blank") String artistId
    ) {
    }
}
