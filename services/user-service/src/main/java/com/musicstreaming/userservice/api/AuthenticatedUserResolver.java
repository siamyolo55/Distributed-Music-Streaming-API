package com.musicstreaming.userservice.api;

import com.musicstreaming.userservice.domain.UserAccountRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticatedUserResolver {

    private final UserAccountRepository userAccountRepository;

    public AuthenticatedUserResolver(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UUID resolveUserId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject != null) {
            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException ignored) {
                // Legacy tokens may use email as subject.
            }

            return userAccountRepository.findByEmailIgnoreCase(subject.trim())
                    .map(account -> account.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject"));
        }

        String emailClaim = jwt.getClaimAsString("email");
        if (emailClaim != null && !emailClaim.isBlank()) {
            return userAccountRepository.findByEmailIgnoreCase(emailClaim.trim())
                    .map(account -> account.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token email claim"));
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject is missing");
    }
}
