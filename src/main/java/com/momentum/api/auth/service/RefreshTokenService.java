package com.momentum.api.auth.service;

import com.momentum.api.auth.exception.InvalidRefreshTokenException;
import com.momentum.api.auth.exception.RefreshTokenReuseDetectedException;
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

    /**
     * Creates the FIRST token in a brand-new family (login / google sign-in)
     */
    @Transactional
    public RefreshToken create(User user) {
        return persistNewToken(user, UUID.randomUUID().toString());
    }

    /**
     * Creates a new token but keeps it tied to an existing family (used during rotation)
     */
    private RefreshToken createInFamily(User user, String familyId) {
        return persistNewToken(user, familyId);
    }

    private RefreshToken persistNewToken(User user, String familyId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .familyId(familyId)
                .expiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validates an incoming refresh token AND performs reuse detection.
     * Throws RefreshTokenReuseDetectedException if the token was already
     * revoked (i.e. it was already rotated once before) - this is the
     * signal that the token may have been stolen.
     */
    @Transactional
    public RefreshToken validateAndDetectReuse(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isExpired()) {
            // Expired - delete and reject it
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException();
        }

        if (refreshToken.isRevoked()) {
            // This exact token was already used once before - REUSE DETECTED.
            // Nuke the entire family - every token issued from this login session.
            refreshTokenRepository.revokeAllByFamilyId(refreshToken.getFamilyId());
            throw new RefreshTokenReuseDetectedException();
        }

        return refreshToken;
    }

    /**
     * Rotates a validated token: marks the old one as revoked (NOT deleted —
     * we need it to remain in DB to detect future reuse) and issues a new
     * token in the same family.
     */
    @Transactional
    public RefreshToken rotate(RefreshToken oldToken) {
        refreshTokenRepository.revokeRefreshTokenById(oldToken.getId());
        return createInFamily(oldToken.getUser(), oldToken.getFamilyId());
    }

    @Transactional
    public void deleteByToken(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(rt -> refreshTokenRepository.deleteAllByUser(rt.getUser()));
    }
}
