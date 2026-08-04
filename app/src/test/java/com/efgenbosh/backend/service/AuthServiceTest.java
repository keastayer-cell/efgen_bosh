package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.AppUser;
import com.efgenbosh.backend.dto.auth.AuthResponse;
import com.efgenbosh.backend.dto.auth.ChangePasswordRequest;
import com.efgenbosh.backend.dto.auth.LoginRequest;
import com.efgenbosh.backend.dto.auth.RegisterRequest;
import com.efgenbosh.backend.repository.AppUserRepository;
import com.efgenbosh.backend.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AccessControlService accessControlService;

    private BCryptPasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("0123456789abcdef0123456789abcdef", 30);
        authService = new AuthService(
            appUserRepository,
            jwtService,
            accessControlService,
            passwordEncoder
        );
    }

    @Test
    void registerCreatesViewerAndReturnsSessionToken() {
        when(appUserRepository.existsByEmailIgnoreCase("new@example.com"))
            .thenReturn(false);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 15L);
            return user;
        });
        when(accessControlService.getRoleCodes(15L)).thenReturn(List.of("VIEWER"));

        RegisterRequest request = new RegisterRequest();
        request.setEmail(" New@Example.com ");
        request.setName(" New User ");
        request.setPassword("secret12");

        AuthResponse response = authService.register(request);

        assertThat(response.userId()).isEqualTo(15L);
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.name()).isEqualTo("New User");
        assertThat(response.roles()).containsExactly("VIEWER");
        verify(accessControlService).assignDefaultUserRole(15L);
    }

    @Test
    void loginNormalizesEmailAndReturnsRoles() {
        AppUser user = user(7L, 3);
        user.setPasswordHash(passwordEncoder.encode("secret12"));
        when(appUserRepository.findByEmailIgnoreCase("user@example.com"))
            .thenReturn(Optional.of(user));
        when(accessControlService.getRoleCodes(7L)).thenReturn(List.of("OPERATOR"));

        LoginRequest request = new LoginRequest();
        request.setEmail(" User@Example.com ");
        request.setPassword("secret12");

        AuthResponse response = authService.login(request);

        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.roles()).containsExactly("OPERATOR");
        assertThat(jwtService.parseToken(response.token()).get("ver", Integer.class))
            .isEqualTo(3);
    }

    @Test
    void passwordChangeInvalidatesEarlierTokens() {
        AppUser user = user(9L, 2);
        user.setPasswordHash(passwordEncoder.encode("secret12"));
        when(appUserRepository.findById(9L)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(accessControlService.getRoleCodes(9L)).thenReturn(List.of("ADMIN"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("secret12");
        request.setNewPassword("new-secret12");
        AppUserPrincipal principal =
            new AppUserPrincipal(9L, user.getEmail(), user.getName(), false, List.of());

        AuthResponse response = authService.changePassword(principal, request);

        assertThat(user.getTokenVersion()).isEqualTo(3);
        assertThat(passwordEncoder.matches("new-secret12", user.getPasswordHash())).isTrue();
        assertThat(jwtService.parseToken(response.token()).get("ver", Integer.class))
            .isEqualTo(3);
    }

    private AppUser user(Long id, int tokenVersion) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail("user@example.com");
        user.setName("Test User");
        user.setTokenVersion(tokenVersion);
        return user;
    }
}
