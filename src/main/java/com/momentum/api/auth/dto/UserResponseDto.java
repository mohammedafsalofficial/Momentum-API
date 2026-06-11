package com.momentum.api.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean accountNonLocked;
    private Instant createdAt;
    private Instant updatedAt;
}
