package com.musicstreaming.userservice.service;

import com.musicstreaming.userservice.domain.UserAccountRepository;
import com.musicstreaming.userservice.domain.UserFollow;
import com.musicstreaming.userservice.domain.UserFollowRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserFollowService {

    private final UserFollowRepository repository;
    private final UserAccountRepository userAccountRepository;

    public UserFollowService(UserFollowRepository repository, UserAccountRepository userAccountRepository) {
        this.repository = repository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public FollowResult followUser(UUID followerUserId, UUID targetUserId) {
        if (followerUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        if (!userAccountRepository.existsById(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found");
        }

        if (repository.existsByFollowerUserIdAndTargetUserId(followerUserId, targetUserId)) {
            return new FollowResult(targetUserId, false);
        }

        repository.save(new UserFollow(followerUserId, targetUserId, Instant.now()));
        return new FollowResult(targetUserId, true);
    }

    @Transactional
    public void unfollowUser(UUID followerUserId, UUID targetUserId) {
        repository.deleteByFollowerUserIdAndTargetUserId(followerUserId, targetUserId);
    }

    @Transactional(readOnly = true)
    public List<FollowedUser> listFollowedUsers(UUID followerUserId) {
        return repository.findAllByFollowerUserIdOrderByCreatedAtDesc(followerUserId).stream()
                .map(follow -> new FollowedUser(follow.getTargetUserId(), follow.getCreatedAt()))
                .toList();
    }

    public record FollowResult(UUID targetUserId, boolean created) {
    }

    public record FollowedUser(UUID targetUserId, Instant followedAt) {
    }
}
