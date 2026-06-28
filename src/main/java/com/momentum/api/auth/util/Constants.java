package com.momentum.api.auth.util;

import java.time.Duration;

public class Constants {

    public static final Duration OTP_TTL = Duration.ofMinutes(15);
    public static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
}
