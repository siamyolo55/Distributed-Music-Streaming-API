package com.musicstreaming.userservice.service;

import com.musicstreaming.userservice.domain.UserAccount;
import com.musicstreaming.userservice.domain.UserAccountRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDirectoryService {

    private final UserAccountRepository userAccountRepository;

    public UserDirectoryService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<DiscoverableUser> listDiscoverableUsers(UUID currentUserId) {
        return userAccountRepository.findAllByIdNotOrderByCreatedAtDesc(currentUserId).stream()
                .map(this::toDiscoverableUser)
                .toList();
    }

    private DiscoverableUser toDiscoverableUser(UserAccount account) {
        return new DiscoverableUser(
                account.getId(),
                account.getDisplayName(),
                account.getEmail(),
                account.getCreatedAt());
    }

    public record DiscoverableUser(
            UUID userId,
            String displayName,
            String email,
            Instant createdAt
    ) {
    }
}
