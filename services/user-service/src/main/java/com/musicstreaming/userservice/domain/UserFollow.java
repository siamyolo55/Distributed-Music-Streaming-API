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
@Table(name = "user_follows")
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "follower_user_id", nullable = false)
    private UUID followerUserId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(nullable = false)
    private Instant createdAt;

    protected UserFollow() {
    }

    public UserFollow(UUID followerUserId, UUID targetUserId, Instant createdAt) {
        this.followerUserId = followerUserId;
        this.targetUserId = targetUserId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFollowerUserId() {
        return followerUserId;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
