package com.marketplace.auth.presentation;

import com.marketplace.auth.application.LoginUseCase;
import com.marketplace.auth.application.RegisterUseCase;
import com.marketplace.auth.application.TokenPair;
import com.marketplace.auth.application.command.LoginCommand;
import com.marketplace.auth.application.command.RegisterCommand;
import com.marketplace.auth.presentation.request.LoginRequest;
import com.marketplace.auth.presentation.request.RegisterRequest;
import com.marketplace.auth.presentation.response.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        var userId = registerUseCase.execute(
                new RegisterCommand(request.email(), request.password(), request.fullName())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", userId.toString(), "message", "Registration successful. Please check your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokens = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(AuthResponse.of(tokens.accessToken(), tokens.refreshToken()));
    }
}
