package com.musicstreaming.userservice.playlist.api;

import com.musicstreaming.userservice.api.AuthenticatedUserResolver;
import com.musicstreaming.userservice.playlist.service.PlaylistService;
import com.musicstreaming.userservice.playlist.service.PlaylistService.PlaylistDetails;
import com.musicstreaming.userservice.playlist.service.PlaylistService.PlaylistTrackInput;
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

@RestController
@RequestMapping("/api/v1/users/me/playlists")
public class UserPlaylistController {

    private final PlaylistService playlistService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public UserPlaylistController(PlaylistService playlistService, AuthenticatedUserResolver authenticatedUserResolver) {
        this.playlistService = playlistService;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PlaylistUpsertRequest request) {
        UUID userId = userIdFromJwt(jwt);
        PlaylistDetails created = playlistService.create(userId, request.name(), request.description(), toTrackInputs(request.tracks()));
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
        PlaylistDetails updated = playlistService.update(userId, playlistId, request.name(), request.description(), toTrackInputs(request.tracks()));
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
        return authenticatedUserResolver.resolveUserId(jwt);
    }

    private PlaylistResponse toResponse(PlaylistDetails details) {
        return new PlaylistResponse(
                details.id().toString(),
                details.name(),
                details.description(),
                details.createdAt(),
                details.updatedAt(),
                details.tracks().stream()
                        .map(track -> new PlaylistTrackResponse(
                                track.trackId(),
                                track.title(),
                                track.artistName(),
                                track.genre(),
                                track.position()))
                        .toList());
    }

    private List<PlaylistTrackInput> toTrackInputs(List<PlaylistTrackRequest> tracks) {
        if (tracks == null) {
            return null;
        }
        return tracks.stream()
                .map(track -> new PlaylistTrackInput(track.trackId(), track.title(), track.artistName(), track.genre()))
                .toList();
    }

    public record PlaylistUpsertRequest(
            @NotBlank(message = "must not be blank")
            @Size(max = 120, message = "must be at most 120 characters")
            String name,
            @Size(max = 600, message = "must be at most 600 characters")
            String description,
            @Valid
            @Size(max = 500, message = "must be at most 500 tracks")
            List<PlaylistTrackRequest> tracks
    ) {
    }

    public record PlaylistTrackRequest(
            @NotBlank(message = "must not be blank")
            @Size(max = 64, message = "must be at most 64 characters")
            String trackId,
            @NotBlank(message = "must not be blank")
            @Size(max = 200, message = "must be at most 200 characters")
            String title,
            @NotBlank(message = "must not be blank")
            @Size(max = 200, message = "must be at most 200 characters")
            String artistName,
            @NotBlank(message = "must not be blank")
            @Size(max = 80, message = "must be at most 80 characters")
            String genre
    ) {
    }

    public record PlaylistResponse(
            String id,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt,
            List<PlaylistTrackResponse> tracks
    ) {
    }

    public record PlaylistTrackResponse(
            String trackId,
            String title,
            String artistName,
            String genre,
            int position
    ) {
    }
}
