package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.RefreshToken;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.TokenExpiredException;
import com.gderuki.taskr.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(@NonNull User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}", user.getUsername());
        return savedToken;
    }

    public Optional<RefreshToken> findByToken(@NonNull String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(@NonNull RefreshToken token) {
        if (token.isExpired()) {
            log.warn("Refresh token expired for user: {}", token.getUser().getUsername());
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token has expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(@NonNull RefreshToken oldToken) {
        log.debug("Rotating refresh token for user: {}", oldToken.getUser().getUsername());
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(oldToken.getUser());
    }

    @Transactional
    public void deleteByUser(@NonNull User user) {
        log.debug("Deleting refresh tokens for user: {}", user.getUsername());
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteByToken(@NonNull String token) {
        log.debug("Deleting refresh token: {}", token);
        refreshTokenRepository.deleteByToken(token);
    }
}
