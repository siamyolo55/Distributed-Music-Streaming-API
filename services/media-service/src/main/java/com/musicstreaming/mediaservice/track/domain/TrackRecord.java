package com.musicstreaming.mediaservice.track.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tracks")
public class TrackRecord {

    @Id
    @Column(name = "track_id", nullable = false, length = 40)
    private String trackId;

    @Column(name = "artist_id", nullable = false, length = 80)
    private String artistId;

    @Column(name = "artist_name", nullable = false, length = 160)
    private String artistName;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 80)
    private String genre;

    @Column(name = "file_url", nullable = false, length = 1024)
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TrackRecord() {
    }

    public TrackRecord(
            String trackId,
            String artistId,
            String artistName,
            String title,
            String genre,
            String fileUrl,
            Instant createdAt) {
        this.trackId = trackId;
        this.artistId = artistId;
        this.artistName = artistName;
        this.title = title;
        this.genre = genre;
        this.fileUrl = fileUrl;
        this.createdAt = createdAt;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
