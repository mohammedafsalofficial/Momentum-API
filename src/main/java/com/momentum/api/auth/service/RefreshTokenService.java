package com.momentum.api.auth.service;

import com.momentum.api.auth.exception.InvalidRefreshTokenException;
import com.momentum.api.auth.model.RefreshToken;
import com.momentum.api.auth.model.User;
import com.momentum.api.auth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token.expiration-days}")
    private long expirationDays;

    public RefreshToken create(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isExpired()) {
            // Expired - delete and reject it
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException();
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotate(RefreshToken oldToken) {
        // Delete old token and issue a new one (rotation)
        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);
        return create(user);
    }

    @Transactional
    public void deleteAllForUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}
