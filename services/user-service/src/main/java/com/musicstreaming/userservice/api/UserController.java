package com.musicstreaming.userservice.api;

import com.musicstreaming.userservice.api.model.RegistrationStatus;
import com.musicstreaming.userservice.service.UserAuthService;
import com.musicstreaming.userservice.service.UserAuthService.RegisteredUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/users")
public class UserController {

    private final UserAuthService userAuthService;

    public UserController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        RegisteredUser user = userAuthService.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserRegistrationResponse(user.id().toString(), user.email(), RegistrationStatus.CREATED.name()));
    }

    public record UserRegistrationRequest(
            @NotBlank(message = "must not be blank")
            @Email(message = "must be a valid email")
            String email,
            @NotBlank(message = "must not be blank")
            String password,
            @NotBlank(message = "must not be blank")
            String displayName
    ) {
    }

    public record UserRegistrationResponse(String userId, String email, String status) {
    }
}
