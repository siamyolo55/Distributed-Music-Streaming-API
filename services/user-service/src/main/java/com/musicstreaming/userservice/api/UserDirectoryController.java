package com.musicstreaming.userservice.api;

import com.musicstreaming.userservice.service.UserDirectoryService;
import com.musicstreaming.userservice.service.UserDirectoryService.DiscoverableUser;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserDirectoryController {

    private final UserDirectoryService userDirectoryService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public UserDirectoryController(
            UserDirectoryService userDirectoryService,
            AuthenticatedUserResolver authenticatedUserResolver) {
        this.userDirectoryService = userDirectoryService;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @GetMapping("/discover")
    public ResponseEntity<List<DiscoverableUserResponse>> discover(@AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = authenticatedUserResolver.resolveUserId(jwt);
        List<DiscoverableUserResponse> response = userDirectoryService.listDiscoverableUsers(currentUserId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private DiscoverableUserResponse toResponse(DiscoverableUser user) {
        return new DiscoverableUserResponse(
                user.userId().toString(),
                user.displayName(),
                user.email(),
                user.createdAt());
    }

    public record DiscoverableUserResponse(
            String userId,
            String displayName,
            String email,
            Instant createdAt
    ) {
    }
}
