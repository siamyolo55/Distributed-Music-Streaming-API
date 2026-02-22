package com.musicstreaming.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musicstreaming.userservice.api.model.OAuthProvider;
import com.musicstreaming.userservice.domain.UserAccount;
import com.musicstreaming.userservice.domain.UserAccountRepository;
import com.musicstreaming.userservice.domain.UserOAuthAccount;
import com.musicstreaming.userservice.domain.UserOAuthAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserOAuthServiceTests {

    @Mock
    private UserOAuthAccountRepository userOAuthAccountRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserOAuthService service;

    @BeforeEach
    void setUp() {
        service = new UserOAuthService(userOAuthAccountRepository, userAccountRepository, passwordEncoder);
    }

    @Test
    void loginUsesExistingOAuthLink() {
        UserAccount user = user("known@example.com", "Known");
        UserOAuthAccount linked = new UserOAuthAccount(user, "google", "oauth-1", "known@example.com", Instant.now());
        when(userOAuthAccountRepository.findByProviderAndProviderUserId("google", "oauth-1"))
                .thenReturn(Optional.of(linked));

        UserOAuthService.OAuthLoginResult result = service.loginOrRegister(
                OAuthProvider.GOOGLE,
                "oauth-1",
                "known@example.com",
                "Known");

        assertThat(result.userCreated()).isFalse();
        assertThat(result.oauthIdentityLinked()).isFalse();
        assertThat(result.user().id()).isEqualTo(user.getId());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
        verify(userOAuthAccountRepository, never()).save(any(UserOAuthAccount.class));
    }

    @Test
    void loginLinksExistingUserByEmail() {
        UserAccount existingUser = user("existing@example.com", "Existing");
        when(userOAuthAccountRepository.findByProviderAndProviderUserId("google", "oauth-2"))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByEmailIgnoreCase("existing@example.com"))
                .thenReturn(Optional.of(existingUser));

        UserOAuthService.OAuthLoginResult result = service.loginOrRegister(
                OAuthProvider.GOOGLE,
                "oauth-2",
                "existing@example.com",
                "Existing");

        assertThat(result.userCreated()).isFalse();
        assertThat(result.oauthIdentityLinked()).isTrue();
        assertThat(result.user().id()).isEqualTo(existingUser.getId());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
        verify(userOAuthAccountRepository).save(any(UserOAuthAccount.class));
    }

    @Test
    void loginCreatesUserWhenEmailMissing() {
        when(userOAuthAccountRepository.findByProviderAndProviderUserId("spotify", "oauth-3"))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByEmailIgnoreCase("newuser@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount created = invocation.getArgument(0);
            ReflectionTestUtils.setField(created, "id", UUID.randomUUID());
            return created;
        });

        UserOAuthService.OAuthLoginResult result = service.loginOrRegister(
                OAuthProvider.SPOTIFY,
                "oauth-3",
                "newuser@example.com",
                "New User");

        assertThat(result.userCreated()).isTrue();
        assertThat(result.oauthIdentityLinked()).isTrue();
        assertThat(result.user().email()).isEqualTo("newuser@example.com");
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(userOAuthAccountRepository).save(any(UserOAuthAccount.class));
    }

    private UserAccount user(String email, String displayName) {
        UserAccount user = new UserAccount(email, "hash", displayName, Instant.now());
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
