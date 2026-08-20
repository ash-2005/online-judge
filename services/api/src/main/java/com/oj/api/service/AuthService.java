package com.oj.api.service;

import com.oj.api.dto.AuthDtos.AuthResponse;
import com.oj.api.dto.AuthDtos.LoginRequest;
import com.oj.api.dto.AuthDtos.RegisterRequest;
import com.oj.api.dto.AuthDtos.UserResponse;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.UserRepository;
import com.oj.api.security.JwtService;
import com.oj.common.entity.User;
import com.oj.common.enums.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("Email already registered");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .dob(request.dob())
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException("User not found"));
        return toAuthResponse(user);
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getDob(),
                user.getRole()
        );
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.createToken(user.getUsername(), user.getId(), user.getRole().name());
        return new AuthResponse(token, toUserResponse(user));
    }
}
