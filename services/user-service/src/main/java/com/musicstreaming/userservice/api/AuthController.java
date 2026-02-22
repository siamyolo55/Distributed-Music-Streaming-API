package com.musicstreaming.userservice.api;

import com.musicstreaming.userservice.api.model.OAuthLoginStatus;
import com.musicstreaming.userservice.api.model.OAuthProvider;
import com.musicstreaming.userservice.api.model.TokenType;
import com.musicstreaming.userservice.config.JwtTokenService;
import com.musicstreaming.userservice.service.UserAuthService;
import com.musicstreaming.userservice.service.UserAuthService.AuthenticatedUser;
import com.musicstreaming.userservice.service.UserOAuthService;
import com.musicstreaming.userservice.service.UserOAuthService.OAuthLoginResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/public/auth")
public class AuthController {

    private final UserAuthService userAuthService;
    private final UserOAuthService userOAuthService;
    private final JwtTokenService jwtTokenService;

    public AuthController(
            UserAuthService userAuthService,
            UserOAuthService userOAuthService,
            JwtTokenService jwtTokenService) {
        this.userAuthService = userAuthService;
        this.userOAuthService = userOAuthService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedUser user = userAuthService.authenticate(request.email(), request.password());
        String token = jwtTokenService.issueToken(user);
        return ResponseEntity.ok(new LoginResponse(token, TokenType.BEARER.value()));
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<OAuthLoginResponse> oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        OAuthProvider provider;
        try {
            provider = OAuthProvider.fromWireValue(request.provider());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported OAuth provider", ex);
        }

        OAuthLoginResult result = userOAuthService.loginOrRegister(
                provider,
                request.providerUserId(),
                request.email(),
                request.displayName());

        String token = jwtTokenService.issueToken(result.user());
        OAuthLoginStatus status = resolveStatus(result);
        return ResponseEntity.ok(new OAuthLoginResponse(token, TokenType.BEARER.value(), result.user().id().toString(), status.name()));
    }

    private OAuthLoginStatus resolveStatus(OAuthLoginResult result) {
        if (result.userCreated()) {
            return OAuthLoginStatus.NEW_USER;
        }
        if (result.oauthIdentityLinked()) {
            return OAuthLoginStatus.LINKED_EXISTING_USER;
        }
        return OAuthLoginStatus.EXISTING_LINK;
    }

    public record LoginRequest(
            @NotBlank(message = "must not be blank")
            @Email(message = "must be a valid email")
            String email,
            @NotBlank(message = "must not be blank")
            String password
    ) {
    }

    public record LoginResponse(String accessToken, String tokenType) {
    }

    public record OAuthLoginRequest(
            @NotBlank(message = "must not be blank")
            String provider,
            @NotBlank(message = "must not be blank")
            String providerUserId,
            @NotBlank(message = "must not be blank")
            @Email(message = "must be a valid email")
            String email,
            @NotBlank(message = "must not be blank")
            String displayName
    ) {
    }

    public record OAuthLoginResponse(
            String accessToken,
            String tokenType,
            String userId,
            String status
    ) {
    }
}
