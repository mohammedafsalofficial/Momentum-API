package com.momentum.api.auth.service;

import com.momentum.api.auth.dto.request.LoginRequest;
import com.momentum.api.auth.dto.request.RegisterRequest;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.exception.EmailAlreadyExistsException;
import com.momentum.api.auth.exception.LoginFailureException;
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

    public UserResponse register(RegisterRequest requestPayload) {
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

        return toUserResponse(savedUser);
    }

    public UserResponse login(LoginRequest requestPayload) {
        User user = userRepository.findByEmail(requestPayload.getEmail()).orElseThrow(LoginFailureException::new);

        if (!passwordEncoder.matches(requestPayload.getPassword(), user.getPassword())) {
            throw new LoginFailureException();
        }

        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
