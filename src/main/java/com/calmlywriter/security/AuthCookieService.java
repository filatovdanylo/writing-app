package com.calmlywriter.security;

import com.calmlywriter.config.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {

    private final JwtProperties jwtProperties;

    public AuthCookieService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String cookieName() {
        return jwtProperties.cookieName();
    }

    public void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.cookieName(), token)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .path("/")
                .maxAge(jwtProperties.expirationMs() / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.cookieName(), "")
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
