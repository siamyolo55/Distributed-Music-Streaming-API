package com.musicstreaming.userservice.service;

import com.musicstreaming.userservice.domain.ArtistFollow;
import com.musicstreaming.userservice.domain.ArtistFollowRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserFollowService {

    private final ArtistFollowRepository repository;

    public UserFollowService(ArtistFollowRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FollowResult followArtist(UUID userId, String artistId) {
        String normalizedArtistId = artistId.trim();
        if (repository.existsByUserIdAndArtistId(userId, normalizedArtistId)) {
            return new FollowResult(normalizedArtistId, false);
        }

        repository.save(new ArtistFollow(userId, normalizedArtistId, Instant.now()));
        return new FollowResult(normalizedArtistId, true);
    }

    @Transactional
    public void unfollowArtist(UUID userId, String artistId) {
        repository.deleteByUserIdAndArtistId(userId, artistId.trim());
    }

    @Transactional(readOnly = true)
    public List<FollowedArtist> listFollowedArtists(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(follow -> new FollowedArtist(follow.getArtistId(), follow.getCreatedAt()))
                .toList();
    }

    public record FollowResult(String artistId, boolean created) {
    }

    public record FollowedArtist(String artistId, Instant followedAt) {
    }
}
