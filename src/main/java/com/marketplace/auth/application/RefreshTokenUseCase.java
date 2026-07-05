package com.marketplace.auth.application;

import com.marketplace.auth.application.port.RefreshTokenStore;
import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.model.UserId;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.auth.infrastructure.adapter.JwtTokenProvider;
import com.marketplace.shared.exception.BusinessException;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public RefreshTokenUseCase(UserRepository userRepository,
                               JwtTokenProvider jwtTokenProvider,
                               RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public TokenPair execute(String refreshToken) {
        Claims claims;
        try {
            claims = jwtTokenProvider.validateAndParse(refreshToken);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
        }

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Token is not a refresh token");
        }

        UserId userId = new UserId(UUID.fromString(claims.getSubject()));

        // Token rotation: chỉ chấp nhận token đang lưu, dùng xong thay mới
        if (!refreshTokenStore.matches(userId, refreshToken)) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token has been revoked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User no longer exists"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        refreshTokenStore.save(userId, newRefreshToken);

        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
