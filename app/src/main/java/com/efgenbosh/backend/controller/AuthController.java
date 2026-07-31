package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.auth.AuthResponse;
import com.efgenbosh.backend.dto.auth.ChangePasswordRequest;
import com.efgenbosh.backend.dto.auth.LoginRequest;
import com.efgenbosh.backend.dto.auth.RegisterRequest;
import com.efgenbosh.backend.dto.auth.UserResponse;
import com.efgenbosh.backend.security.AppUserPrincipal;
import com.efgenbosh.backend.service.AuthCookieService;
import com.efgenbosh.backend.service.AuthService;
import com.efgenbosh.backend.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieService authCookieService;
    private final boolean trustForwardHeaders;

    public AuthController(
        AuthService authService,
        RefreshTokenService refreshTokenService,
        AuthCookieService authCookieService,
        @Value("${APP_TRUST_FORWARD_HEADERS:false}") boolean trustForwardHeaders
    ) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.authCookieService = authCookieService;
        this.trustForwardHeaders = trustForwardHeaders;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("headerName", csrfToken.getHeaderName(), "token", csrfToken.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) {
        return buildSessionResponse(
            authService.register(request),
            httpRequest,
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        return buildSessionResponse(
            authService.login(request),
            httpRequest,
            HttpStatus.OK
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest
    ) {
        return buildSessionResponse(
            authService.changePassword(requirePrincipal(authentication), request),
            httpRequest,
            HttpStatus.OK
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
        @CookieValue(
            value = AuthCookieService.REFRESH_TOKEN_COOKIE_NAME,
            required = false
        ) String refreshToken,
        HttpServletRequest httpRequest
    ) {
        RefreshTokenService.RefreshTokenRotation rotation =
            refreshTokenService.rotateToken(
                refreshToken,
                httpRequest.getHeader("User-Agent"),
                extractClientIp(httpRequest)
            );
        return ResponseEntity.ok()
            .header(
                HttpHeaders.SET_COOKIE,
                authCookieService.buildRefreshTokenCookie(rotation.refreshToken())
            )
            .body(authService.buildAuthResponseForUserId(rotation.userId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(
            value = AuthCookieService.REFRESH_TOKEN_COOKIE_NAME,
            required = false
        ) String refreshToken
    ) {
        refreshTokenService.revoke(refreshToken);
        return ResponseEntity.noContent()
            .header(
                HttpHeaders.SET_COOKIE,
                authCookieService.buildClearRefreshTokenCookie()
            )
            .build();
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getCurrentUser(requirePrincipal(authentication));
    }

    private ResponseEntity<AuthResponse> buildSessionResponse(
        AuthResponse response,
        HttpServletRequest request,
        HttpStatus status
    ) {
        String refreshToken = refreshTokenService.issueTokenForUserId(
            response.userId(),
            request.getHeader("User-Agent"),
            extractClientIp(request)
        );
        return ResponseEntity.status(status)
            .header(
                HttpHeaders.SET_COOKIE,
                authCookieService.buildRefreshTokenCookie(refreshToken)
            )
            .body(response);
    }

    private AppUserPrincipal requirePrincipal(Authentication authentication) {
        if (
            authentication == null
                || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)
        ) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Не удалось определить пользователя из токена."
            );
        }
        return principal;
    }

    private String extractClientIp(HttpServletRequest request) {
        if (!trustForwardHeaders) {
            return request.getRemoteAddr();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String candidate = forwardedFor.split(",")[0].trim();
            if (
                !candidate.isEmpty()
                    && candidate.length() <= 64
                    && candidate.chars().noneMatch(Character::isWhitespace)
            ) {
                return candidate;
            }
        }
        return request.getRemoteAddr();
    }
}
