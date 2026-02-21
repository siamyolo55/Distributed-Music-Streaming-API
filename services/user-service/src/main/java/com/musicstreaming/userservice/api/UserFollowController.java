package com.musicstreaming.userservice.api;

import com.musicstreaming.userservice.api.model.FollowStatus;
import com.musicstreaming.userservice.service.UserFollowService;
import com.musicstreaming.userservice.service.UserFollowService.FollowResult;
import com.musicstreaming.userservice.service.UserFollowService.FollowedArtist;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users/me/follows")
public class UserFollowController {

    private final UserFollowService userFollowService;

    public UserFollowController(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    @PostMapping("/{artistId}")
    public ResponseEntity<FollowResponse> followArtist(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("artistId") @NotBlank(message = "must not be blank") String artistId) {
        UUID userId = userIdFromJwt(jwt);
        FollowResult result = userFollowService.followArtist(userId, artistId);
        FollowStatus followStatus = result.created() ? FollowStatus.FOLLOWED : FollowStatus.ALREADY_FOLLOWING;
        FollowResponse response = new FollowResponse(result.artistId(), followStatus.name());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> unfollowArtist(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("artistId") @NotBlank(message = "must not be blank") String artistId) {
        UUID userId = userIdFromJwt(jwt);
        userFollowService.unfollowArtist(userId, artistId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FollowedArtistResponse>> listFollowedArtists(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userIdFromJwt(jwt);
        List<FollowedArtistResponse> response = userFollowService.listFollowedArtists(userId).stream()
                .map(f -> new FollowedArtistResponse(f.artistId(), f.followedAt()))
                .toList();
        return ResponseEntity.ok(response);
    }

    private UUID userIdFromJwt(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject", ex);
        }
    }

    public record FollowResponse(String artistId, String status) {
    }

    public record FollowedArtistResponse(String artistId, java.time.Instant followedAt) {
    }
}
