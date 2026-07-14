package com.momentum.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.api.common.response.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ThrottlingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> bucketProxyManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String ip = extractClientIp(request);
        String key = "rl:" + request.getRequestURI() + ":" + ip;

        BucketConfiguration bucketConfiguration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillGreedy(5, Duration.ofMinutes(1))
                        .build())
                .build();

        Bucket bucket = bucketProxyManager.builder().build(key, () -> bucketConfiguration);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            ErrorResponse responsePayload = ErrorResponse.builder()
                    .success(false)
                    .message("Too many requests. Try again later.")
                    .errors(List.of())
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(responsePayload));
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ?
                forwarded.split(",")[0].trim() :
                request.getRemoteAddr();
    }
}
