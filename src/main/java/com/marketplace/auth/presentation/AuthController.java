package com.marketplace.auth.presentation;

import com.marketplace.auth.application.LoginUseCase;
import com.marketplace.auth.application.RefreshTokenUseCase;
import com.marketplace.auth.application.RegisterUseCase;
import com.marketplace.auth.application.TokenPair;
import com.marketplace.auth.application.VerifyEmailUseCase;
import com.marketplace.auth.application.command.LoginCommand;
import com.marketplace.auth.application.command.RegisterCommand;
import com.marketplace.auth.application.command.VerifyEmailCommand;
import com.marketplace.auth.presentation.request.LoginRequest;
import com.marketplace.auth.presentation.request.RefreshTokenRequest;
import com.marketplace.auth.presentation.request.RegisterRequest;
import com.marketplace.auth.presentation.request.VerifyEmailRequest;
import com.marketplace.auth.presentation.response.AuthResponse;
import com.marketplace.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          RefreshTokenUseCase refreshTokenUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        var userId = registerUseCase.execute(
                new RegisterCommand(request.email(), request.password(), request.fullName())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", userId.toString(), "message", "Registration successful. Please check your email."));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.execute(new VerifyEmailCommand(request.email(), request.otp()));
        return ResponseEntity.ok(Map.of("message", "Email verified. You can now log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokens = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(AuthResponse.of(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPair tokens = refreshTokenUseCase.execute(request.refreshToken());
        return ResponseEntity.ok(AuthResponse.of(tokens.accessToken(), tokens.refreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUser> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(user);
    }
}
