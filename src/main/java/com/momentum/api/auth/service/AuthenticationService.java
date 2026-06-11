package com.momentum.api.auth.service;

import com.momentum.api.auth.dto.RegisterRequestDto;
import com.momentum.api.auth.dto.UserResponseDto;
import com.momentum.api.auth.exception.EmailAlreadyExistsException;
import com.momentum.api.auth.model.User;
import com.momentum.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto register(RegisterRequestDto requestPayload) {
        if (userRepository.existsByEmail(requestPayload.getEmail())) {
            throw new EmailAlreadyExistsException(requestPayload.getEmail());
        }

        User user = User.builder()
                .email(requestPayload.getEmail())
                .password(passwordEncoder.encode(requestPayload.getPassword()))
                .firstName(requestPayload.getFirstName())
                .lastName(requestPayload.getLastName())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .enabled(savedUser.isEnabled())
                .accountNonLocked(savedUser.isAccountNonLocked())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }
}
