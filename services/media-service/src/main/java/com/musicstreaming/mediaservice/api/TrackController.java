package com.musicstreaming.mediaservice.api;

import com.musicstreaming.common.events.EventEnvelope;
import com.musicstreaming.common.events.TrackUploadedEvent;
import jakarta.validation.constraints.NotBlank;
import com.musicstreaming.mediaservice.track.TrackIngestionService;
import com.musicstreaming.mediaservice.track.TrackView;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media/tracks")
@Validated
public class TrackController {

    private final TrackIngestionService trackIngestionService;

    public TrackController(TrackIngestionService trackIngestionService) {
        this.trackIngestionService = trackIngestionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventEnvelope<TrackUploadedEvent>> upload(
            @RequestParam("title") @NotBlank(message = "must not be blank") String title,
            @RequestParam("artistId") @NotBlank(message = "must not be blank") String artistId,
            @RequestParam("artistName") @NotBlank(message = "must not be blank") String artistName,
            @RequestParam("genre") @NotBlank(message = "must not be blank") String genre,
            @RequestParam("file") MultipartFile file) {
        EventEnvelope<TrackUploadedEvent> envelope = trackIngestionService.ingest(title, artistId, artistName, genre, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(envelope);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrackView>> listTracks() {
        return ResponseEntity.ok(trackIngestionService.listTracks());
    }
}
