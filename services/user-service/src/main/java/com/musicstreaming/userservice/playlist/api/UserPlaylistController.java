package com.musicstreaming.userservice.playlist.api;

import com.musicstreaming.userservice.playlist.service.PlaylistService;
import com.musicstreaming.userservice.playlist.service.PlaylistService.PlaylistDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users/me/playlists")
public class UserPlaylistController {

    private final PlaylistService playlistService;

    public UserPlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PlaylistUpsertRequest request) {
        UUID userId = userIdFromJwt(jwt);
        PlaylistDetails created = playlistService.create(userId, request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userIdFromJwt(jwt);
        List<PlaylistResponse> response = playlistService.list(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> get(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("playlistId") UUID playlistId) {
        UUID userId = userIdFromJwt(jwt);
        PlaylistDetails playlist = playlistService.get(userId, playlistId);
        return ResponseEntity.ok(toResponse(playlist));
    }

    @PutMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("playlistId") UUID playlistId,
            @Valid @RequestBody PlaylistUpsertRequest request) {
        UUID userId = userIdFromJwt(jwt);
        PlaylistDetails updated = playlistService.update(userId, playlistId, request.name(), request.description());
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("playlistId") UUID playlistId) {
        UUID userId = userIdFromJwt(jwt);
        playlistService.delete(userId, playlistId);
        return ResponseEntity.noContent().build();
    }

    private UUID userIdFromJwt(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject", ex);
        }
    }

    private PlaylistResponse toResponse(PlaylistDetails details) {
        return new PlaylistResponse(
                details.id().toString(),
                details.name(),
                details.description(),
                details.createdAt(),
                details.updatedAt());
    }

    public record PlaylistUpsertRequest(
            @NotBlank(message = "must not be blank")
            @Size(max = 120, message = "must be at most 120 characters")
            String name,
            @Size(max = 600, message = "must be at most 600 characters")
            String description
    ) {
    }

    public record PlaylistResponse(
            String id,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
