package com.momentum.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_PAYLOAD_LENGTH = 1000;
    private static final int CACHE_LIMIT = 4096; // bytes cached from the request body - 4096 bytes -> 4kb

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        // Wrap so we can read body content after it's consumed downstream
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestId, wrappedRequest, wrappedResponse, duration);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(
            String requestId, ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response, long duration) {
        String ip = extractClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        int status = response.getStatus();

        log.info("[{}] {} {}{} from {} -> {} ({}ms)",
                requestId, method, uri, query != null ? "?" + query : "", ip, status, duration);

        if (log.isDebugEnabled()) {
            String requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
            log.debug("[{}] Request body: {}", requestId, sanitize(uri, requestBody));
            log.debug("[{}] Response body: {}", requestId, sanitize(uri, responseBody));
        }
    }

    private String getContentAsString(byte[] content, String encoding) {
        if (content == null || content.length == 0) return "";
        try {
            String body = new String(content, encoding != null ? encoding : "UTF-8");
            return body.length() > MAX_PAYLOAD_LENGTH ? body.substring(0, MAX_PAYLOAD_LENGTH) + "...[TRUNCATED]" : body;
        } catch (Exception e) {
            return "[UNREADABLE]";
        }
    }

    // Redact sensitive fields on auth endpoints instead of logging raw passwords/tokens
    private String sanitize(String uri, String body) {
        if (uri.contains("/api/auth/") && body != null) {
            return body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[REDACTED]\"")
                    .replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"[REDACTED]\"")
                    .replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"[REDACTED]\"");
        }
        return body;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ?
                forwarded.split(",")[0].trim() :
                request.getRemoteAddr();
    }
}
