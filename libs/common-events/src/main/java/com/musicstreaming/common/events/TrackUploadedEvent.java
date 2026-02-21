package com.musicstreaming.common.events;

public record TrackUploadedEvent(
        String trackId,
        String artistId,
        String title,
        String storagePath,
        String schemaVersion
) {
}
