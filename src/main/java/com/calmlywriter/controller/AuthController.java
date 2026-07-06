package com.calmlywriter.controller;

import com.calmlywriter.dto.AuthResponse;
import com.calmlywriter.dto.AuthResult;
import com.calmlywriter.dto.LoginRequest;
import com.calmlywriter.dto.RegisterRequest;
import com.calmlywriter.security.AuthCookieService;
import com.calmlywriter.security.UserPrincipal;
import com.calmlywriter.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return authenticate(authService.register(request), response);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return authenticate(authService.login(request), response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return new AuthResponse(principal.userId(), principal.email());
    }

    private AuthResponse authenticate(AuthResult result, HttpServletResponse response) {
        authCookieService.setAuthCookie(response, result.token());
        return result.response();
    }
}
