package com.momentum.api.common.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class SuccessResponse<T> {

    private boolean success;

    private String message;

    private T data;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
