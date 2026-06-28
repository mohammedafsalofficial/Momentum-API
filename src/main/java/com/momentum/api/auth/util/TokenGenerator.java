package com.momentum.api.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    private TokenGenerator() {}

    /**
     * Generates a cryptographically secure 6-digit OTP e.g. "048273"
     */
    public static String generateOtp() {
        int otp = secureRandom.nextInt(1_000_000);  // 0 to 999999
        return String.format("%06d", otp);  // zero-pad to always be 6 digits
    }

    /**
     * Opaque random token for password reset authorization
     */
    public static String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
