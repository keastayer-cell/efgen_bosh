package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.AppUser;
import com.efgenbosh.backend.dto.auth.AuthResponse;
import com.efgenbosh.backend.dto.auth.ChangePasswordRequest;
import com.efgenbosh.backend.dto.auth.LoginRequest;
import com.efgenbosh.backend.dto.auth.RegisterRequest;
import com.efgenbosh.backend.dto.auth.UserResponse;
import com.efgenbosh.backend.repository.AppUserRepository;
import com.efgenbosh.backend.security.AppUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final AccessControlService accessControlService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(
        AppUserRepository appUserRepository,
        JwtService jwtService,
        AccessControlService accessControlService,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
        this.accessControlService = accessControlService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                "Пользователь с таким email уже существует."
            );
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setName(request.getName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setMustChangePassword(false);
        user.setTokenVersion(0);
        user.setPasswordChangedAt(OffsetDateTime.now());

        AppUser saved = appUserRepository.save(user);
        accessControlService.assignDefaultUserRole(saved.getId());
        return buildAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
            .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль.");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse changePassword(
        AppUserPrincipal principal,
        ChangePasswordRequest request
    ) {
        AppUser user = findUser(principal.getUserId());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Текущий пароль указан неверно.");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("Новый пароль должен отличаться от текущего.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        user.setPasswordChangedAt(OffsetDateTime.now());
        return buildAuthResponse(appUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(AppUserPrincipal principal) {
        AppUser user = findUser(principal.getUserId());
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            accessControlService.getRoleCodes(user.getId()),
            user.isMustChangePassword()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse buildAuthResponseForUserId(Long userId) {
        return buildAuthResponse(findUser(userId));
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Пользователь больше не найден. Войдите снова."
            ));
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        String token = jwtService.generateToken(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getTokenVersion()
        );
        List<String> roles = accessControlService.getRoleCodes(user.getId());
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getName(),
            roles,
            user.isMustChangePassword()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
