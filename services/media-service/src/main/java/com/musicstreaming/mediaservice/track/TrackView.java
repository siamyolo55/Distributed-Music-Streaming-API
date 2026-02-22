package com.musicstreaming.mediaservice.track;

import java.time.Instant;

public record TrackView(
        String trackId,
        String artistId,
        String artistName,
        String title,
        String genre,
        String fileUrl,
        Instant createdAt
) {
}
