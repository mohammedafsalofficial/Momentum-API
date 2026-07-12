package com.momentum.api.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // Must be a Base64-encoded string containing at least 256 bits (32 bytes) of data
    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration:900000}")  // Default 15 minutes
    private long jwtExpirationMs;

    private static final String AUTHORITIES_KEY = "auth";

    private final static Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Reusable helper to generate the SecretKey matching modern cryptographic standards
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate token with custom claims based on Spring Security's UserDetails
     */
    public String generateToken(UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        claims.put(AUTHORITIES_KEY, authorities);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extract username (subject) safely from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract specific components out of the token payloads
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if the token is both structure-valid and belongs to the logging-in user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Extract jti from a token
     */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = extractAllClaims(token);
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(claims.get(AUTHORITIES_KEY).toString().split(",")).stream()
                .map(SimpleGrantedAuthority::new).toList();
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
