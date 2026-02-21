package com.musicstreaming.userservice.service;

import com.musicstreaming.userservice.domain.UserAccount;
import com.musicstreaming.userservice.domain.UserAccountRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisteredUser register(String email, String rawPassword, String displayName) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        UserAccount saved = repository.save(new UserAccount(normalizedEmail, passwordHash, displayName.trim(), Instant.now()));
        return new RegisteredUser(saved.getId(), saved.getEmail(), saved.getDisplayName());
    }

    public AuthenticatedUser authenticate(String email, String rawPassword) {
        UserAccount user = repository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getDisplayName());
    }

    public record RegisteredUser(UUID id, String email, String displayName) {
    }

    public record AuthenticatedUser(UUID id, String email, String displayName) {
    }
}
