package com.musicstreaming.userservice.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    boolean existsByFollowerUserIdAndTargetUserId(UUID followerUserId, UUID targetUserId);

    void deleteByFollowerUserIdAndTargetUserId(UUID followerUserId, UUID targetUserId);

    List<UserFollow> findAllByFollowerUserIdOrderByCreatedAtDesc(UUID followerUserId);
}
