package com.momentum.api.auth.filter;

import com.momentum.api.auth.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = resolveToken(request);
            if (StringUtils.hasText(jwt)) {
                Authentication authentication = jwtUtil.getAuthentication(jwt);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            LOGGER.info("Security exception for user {} - {}", e.getClaims().getSubject(), e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void resetAuthenticationAfterRequest() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorizationReqHeader = request.getHeader("Authorization");

        log.debug("AUTHORIZATION HEADER: {}", authorizationReqHeader);

        if (StringUtils.hasText(authorizationReqHeader) && authorizationReqHeader.startsWith("Bearer ")) {
            return authorizationReqHeader.substring(7);
        }
        return null;
    }
}
