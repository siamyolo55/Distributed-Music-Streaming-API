package com.musicstreaming.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musicstreaming.userservice.domain.ArtistFollow;
import com.musicstreaming.userservice.domain.ArtistFollowRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTests {

    @Mock
    private ArtistFollowRepository repository;

    private UserFollowService service;

    @BeforeEach
    void setUp() {
        service = new UserFollowService(repository);
    }

    @Test
    void followArtistReturnsCreatedWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(repository.existsByUserIdAndArtistId(userId, "artist-1")).thenReturn(false);

        UserFollowService.FollowResult result = service.followArtist(userId, "artist-1");

        assertThat(result.created()).isTrue();
        assertThat(result.artistId()).isEqualTo("artist-1");
        verify(repository).save(org.mockito.ArgumentMatchers.any(ArtistFollow.class));
    }

    @Test
    void followArtistIsIdempotentWhenAlreadyFollowing() {
        UUID userId = UUID.randomUUID();
        when(repository.existsByUserIdAndArtistId(userId, "artist-1")).thenReturn(true);

        UserFollowService.FollowResult result = service.followArtist(userId, "artist-1");

        assertThat(result.created()).isFalse();
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(ArtistFollow.class));
    }

    @Test
    void listFollowedArtistsMapsRepositoryRecords() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        when(repository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(new ArtistFollow(userId, "artist-1", now)));

        List<UserFollowService.FollowedArtist> result = service.listFollowedArtists(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().artistId()).isEqualTo("artist-1");
        assertThat(result.getFirst().followedAt()).isEqualTo(now);
    }
}
