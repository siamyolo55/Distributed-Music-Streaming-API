package com.musicstreaming.userservice.api;

import com.musicstreaming.userservice.config.JwtTokenService;
import com.musicstreaming.userservice.service.UserAuthService;
import com.musicstreaming.userservice.service.UserAuthService.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/auth")
public class AuthController {

    private final UserAuthService userAuthService;
    private final JwtTokenService jwtTokenService;

    public AuthController(UserAuthService userAuthService, JwtTokenService jwtTokenService) {
        this.userAuthService = userAuthService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedUser user = userAuthService.authenticate(request.email(), request.password());
        String token = jwtTokenService.issueToken(user);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
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
}
