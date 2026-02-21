package com.musicstreaming.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musicstreaming.userservice.domain.UserAccountRepository;
import com.musicstreaming.userservice.domain.UserFollow;
import com.musicstreaming.userservice.domain.UserFollowRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTests {

    @Mock
    private UserFollowRepository repository;

    @Mock
    private UserAccountRepository userAccountRepository;

    private UserFollowService service;

    @BeforeEach
    void setUp() {
        service = new UserFollowService(repository, userAccountRepository);
    }

    @Test
    void followUserReturnsCreatedWhenMissing() {
        UUID followerUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        when(userAccountRepository.existsById(targetUserId)).thenReturn(true);
        when(repository.existsByFollowerUserIdAndTargetUserId(followerUserId, targetUserId)).thenReturn(false);

        UserFollowService.FollowResult result = service.followUser(followerUserId, targetUserId);

        assertThat(result.created()).isTrue();
        assertThat(result.targetUserId()).isEqualTo(targetUserId);
        verify(repository).save(org.mockito.ArgumentMatchers.any(UserFollow.class));
    }

    @Test
    void followUserIsIdempotentWhenAlreadyFollowing() {
        UUID followerUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        when(userAccountRepository.existsById(targetUserId)).thenReturn(true);
        when(repository.existsByFollowerUserIdAndTargetUserId(followerUserId, targetUserId)).thenReturn(true);

        UserFollowService.FollowResult result = service.followUser(followerUserId, targetUserId);

        assertThat(result.created()).isFalse();
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(UserFollow.class));
    }

    @Test
    void followUserRejectsSelfFollow() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> service.followUser(userId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You cannot follow yourself");
    }

    @Test
    void followUserReturnsNotFoundWhenTargetMissing() {
        UUID followerUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        when(userAccountRepository.existsById(targetUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.followUser(followerUserId, targetUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Target user not found");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(UserFollow.class));
    }

    @Test
    void listFollowedUsersMapsRepositoryRecords() {
        UUID followerUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Instant now = Instant.now();
        when(repository.findAllByFollowerUserIdOrderByCreatedAtDesc(followerUserId))
                .thenReturn(List.of(new UserFollow(followerUserId, targetUserId, now)));

        List<UserFollowService.FollowedUser> result = service.listFollowedUsers(followerUserId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().targetUserId()).isEqualTo(targetUserId);
        assertThat(result.getFirst().followedAt()).isEqualTo(now);
    }
}
