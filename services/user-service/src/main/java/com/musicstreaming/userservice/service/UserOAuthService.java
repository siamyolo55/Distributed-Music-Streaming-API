package com.musicstreaming.userservice.service;

import com.musicstreaming.userservice.api.model.OAuthProvider;
import com.musicstreaming.userservice.domain.UserAccount;
import com.musicstreaming.userservice.domain.UserAccountRepository;
import com.musicstreaming.userservice.domain.UserOAuthAccount;
import com.musicstreaming.userservice.domain.UserOAuthAccountRepository;
import com.musicstreaming.userservice.service.UserAuthService.AuthenticatedUser;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserOAuthService {

    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserOAuthService(
            UserOAuthAccountRepository userOAuthAccountRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OAuthLoginResult loginOrRegister(
            OAuthProvider provider,
            String providerUserId,
            String email,
            String displayName) {
        String normalizedProviderUserId = providerUserId.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedDisplayName = displayName.trim();

        UserOAuthAccount existingOAuthAccount = userOAuthAccountRepository
                .findByProviderAndProviderUserId(provider.wireValue(), normalizedProviderUserId)
                .orElse(null);
        if (existingOAuthAccount != null) {
            UserAccount existingUser = existingOAuthAccount.getUser();
            return new OAuthLoginResult(
                    new AuthenticatedUser(existingUser.getId(), existingUser.getEmail(), existingUser.getDisplayName()),
                    false,
                    false);
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        boolean userCreated = false;
        if (user == null) {
            String generatedPassword = "oauth-" + UUID.randomUUID();
            user = userAccountRepository.save(new UserAccount(
                    normalizedEmail,
                    passwordEncoder.encode(generatedPassword),
                    normalizedDisplayName,
                    Instant.now()));
            userCreated = true;
        }

        userOAuthAccountRepository.save(new UserOAuthAccount(
                user,
                provider.wireValue(),
                normalizedProviderUserId,
                normalizedEmail,
                Instant.now()));

        return new OAuthLoginResult(
                new AuthenticatedUser(user.getId(), user.getEmail(), user.getDisplayName()),
                userCreated,
                true);
    }

    public record OAuthLoginResult(AuthenticatedUser user, boolean userCreated, boolean oauthIdentityLinked) {
    }
}
