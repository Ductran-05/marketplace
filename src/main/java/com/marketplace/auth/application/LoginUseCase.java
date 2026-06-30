package com.marketplace.auth.application;

import com.marketplace.auth.application.command.LoginCommand;
import com.marketplace.auth.domain.model.Email;
import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.auth.infrastructure.adapter.JwtTokenProvider;
import com.marketplace.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

public record TokenPair(String accessToken, String refreshToken) {}

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginUseCase(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenPair execute(LoginCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!user.isActive()) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Account is not activated");
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        return new TokenPair(accessToken, refreshToken);
    }
}
