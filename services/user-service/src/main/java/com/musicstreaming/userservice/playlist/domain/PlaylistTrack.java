package com.musicstreaming.userservice.playlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "playlist_tracks")
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "playlist_id", nullable = false)
    private UUID playlistId;

    @Column(name = "track_id", nullable = false, length = 64)
    private String trackId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "artist_name", nullable = false, length = 200)
    private String artistName;

    @Column(nullable = false, length = 80)
    private String genre;

    @Column(nullable = false)
    private int position;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected PlaylistTrack() {
    }

    public PlaylistTrack(UUID playlistId, String trackId, String title, String artistName, String genre, int position, Instant addedAt) {
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.title = title;
        this.artistName = artistName;
        this.genre = genre;
        this.position = position;
        this.addedAt = addedAt;
    }

    public UUID getPlaylistId() {
        return playlistId;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getGenre() {
        return genre;
    }

    public int getPosition() {
        return position;
    }
}
