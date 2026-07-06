package com.calmlywriter.service;

import com.calmlywriter.dto.AuthResponse;
import com.calmlywriter.dto.AuthResult;
import com.calmlywriter.dto.LoginRequest;
import com.calmlywriter.dto.RegisterRequest;
import com.calmlywriter.entity.User;
import com.calmlywriter.exception.BadRequestException;
import com.calmlywriter.exception.UnauthorizedException;
import com.calmlywriter.repository.UserRepository;
import com.calmlywriter.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return buildAuthResult(user);
    }

    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return buildAuthResult(user);
    }

    private AuthResult buildAuthResult(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        AuthResponse response = new AuthResponse(user.getId(), user.getEmail());
        return new AuthResult(response, token);
    }
}
