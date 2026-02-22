package com.musicstreaming.userservice.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, UUID> {
    Optional<UserOAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
