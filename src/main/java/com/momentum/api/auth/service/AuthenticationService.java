package com.momentum.api.auth.service;

import com.momentum.api.auth.dto.request.LoginRequest;
import com.momentum.api.auth.dto.request.RegisterRequest;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.enums.UserRole;
import com.momentum.api.auth.exception.EmailAlreadyExistsException;
import com.momentum.api.auth.exception.LoginFailureException;
import com.momentum.api.auth.model.User;
import com.momentum.api.auth.repository.UserRepository;
import com.momentum.api.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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

    public String login(LoginRequest requestPayload) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestPayload.getEmail(), requestPayload.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            return jwtUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            throw new LoginFailureException();
        }
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
