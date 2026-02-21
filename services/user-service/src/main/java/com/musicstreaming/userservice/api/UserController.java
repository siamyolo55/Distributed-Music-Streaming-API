package com.musicstreaming.userservice.api;

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

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        String userId = "usr_" + request.email().hashCode();
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserRegistrationResponse(userId, request.email(), "CREATED"));
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
