package com.musicstreaming.userservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artist_follows")
public class ArtistFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "artist_id", nullable = false, length = 120)
    private String artistId;

    @Column(nullable = false)
    private Instant createdAt;

    protected ArtistFollow() {
    }

    public ArtistFollow(UUID userId, String artistId, Instant createdAt) {
        this.userId = userId;
        this.artistId = artistId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getArtistId() {
        return artistId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
