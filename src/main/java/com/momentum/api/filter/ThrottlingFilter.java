package com.momentum.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.api.common.response.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
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
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ThrottlingFilter extends OncePerRequestFilter {

    private static final long CAPACITY = 5;
    private static final long REFILL_TOKENS = 5;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    private static final long NANOS_PER_TOKEN = REFILL_PERIOD.toNanos() / REFILL_TOKENS;

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
                        .capacity(CAPACITY)
                        .refillGreedy(REFILL_TOKENS, REFILL_PERIOD)
                        .build())
                .build();

        Bucket bucket = bucketProxyManager.builder().build(key, () -> bucketConfiguration);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long remainingTokens = probe.getRemainingTokens();
        long epochSecondsToResetBucket = computeEpochSecondsToResetBucket(remainingTokens, probe.getNanosToWaitForRefill());

        response.addHeader("X-RateLimit-Limit", String.valueOf(CAPACITY));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
        response.addHeader("X-RateLimit-Reset", String.valueOf(epochSecondsToResetBucket));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            // round up so we never tell the client to retry too early
            if (TimeUnit.SECONDS.toNanos(waitSeconds) < probe.getNanosToWaitForRefill()) {
                waitSeconds++;
            }

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(waitSeconds));

            ErrorResponse responsePayload = ErrorResponse.builder()
                    .success(false)
                    .message("Too many requests. Try again later.")
                    .errors(List.of())
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(responsePayload));
        }
    }

    /**
     * Computes the epoch-second timestamp at which the bucket will be fully
     * refilled back to capacity. If tokens remain, nanosToWaitForRefill is 0
     * (bucket4j only reports wait time when a token is unavailable), so we
     * derive full-reset time from how many tokens are missing and the
     * per-token refill rate.
     */
    private long computeEpochSecondsToResetBucket(long remainingTokens, long nanosToWaitForRefill) {
        long tokensMissing = CAPACITY - remainingTokens;
        long nanosUntilFull = (tokensMissing > 0)
                ? Math.max(nanosToWaitForRefill, tokensMissing * NANOS_PER_TOKEN)
                : 0;
        long secondsUntilFull = ceilNanosToSeconds(nanosUntilFull);
        return Instant.now().plusSeconds(secondsUntilFull).getEpochSecond();
    }

    private long ceilNanosToSeconds(long nanos) {
        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos);
        if (TimeUnit.SECONDS.toNanos(seconds) < nanos) {
            seconds++;
        }
        return seconds;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ?
                forwarded.split(",")[0].trim() :
                request.getRemoteAddr();
    }
}
