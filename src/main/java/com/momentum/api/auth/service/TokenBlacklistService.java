package com.momentum.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Adds a JWT to the blacklist.
     * TTL is set to the token's remaining lifetime so Redis auto-cleans
     * entries for already-expired tokens — no manual cleanup needed.
     *
     * @param jti        the unique JWT ID (jti claim)
     * @param expiration the token's expiration date
     */
    public void blacklist(String jti, Date expiration) {
        Duration remainingTtl = Duration.between(Instant.now(), expiration.toInstant());
        if (!remainingTtl.isNegative())
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "revoked", remainingTtl);
    }

    /**
     * Returns true if the given jti has been blacklisted.
     *
     * @param jti the unique JWT ID to check
     */
    public boolean isBlacklist(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
