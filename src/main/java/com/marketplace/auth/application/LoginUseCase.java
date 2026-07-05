package com.marketplace.auth.application;

import com.marketplace.auth.application.command.LoginCommand;
import com.marketplace.auth.application.port.RefreshTokenStore;
import com.marketplace.auth.domain.model.Email;
import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.auth.infrastructure.adapter.JwtTokenProvider;
import com.marketplace.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public LoginUseCase(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
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
        refreshTokenStore.save(user.getId(), refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }
}
