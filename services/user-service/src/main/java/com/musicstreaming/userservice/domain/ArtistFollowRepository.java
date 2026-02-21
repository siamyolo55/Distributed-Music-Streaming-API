package com.musicstreaming.userservice.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, UUID> {
    boolean existsByUserIdAndArtistId(UUID userId, String artistId);

    void deleteByUserIdAndArtistId(UUID userId, String artistId);

    List<ArtistFollow> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
