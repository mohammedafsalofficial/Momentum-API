package com.momentum.api.common.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieService {

    public ResponseCookie buildRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    public ResponseCookie buildAccessTokenCookie(String token) {
        return ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie createCookie(String cookieName, String cookieValue, String path, long maxAgeInMinutes) {
        return ResponseCookie.from(cookieName, cookieValue)
                .httpOnly(true)
                .secure(false)
                .path(path)
                .maxAge(Duration.ofMinutes(maxAgeInMinutes))
                .sameSite("Strict")
                .build();
    }
}
