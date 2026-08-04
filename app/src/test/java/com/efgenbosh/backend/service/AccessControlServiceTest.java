package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.AppUser;
import com.efgenbosh.backend.domain.Role;
import com.efgenbosh.backend.domain.RoleCode;
import com.efgenbosh.backend.domain.UserRole;
import com.efgenbosh.backend.repository.AppUserRepository;
import com.efgenbosh.backend.repository.RoleRepository;
import com.efgenbosh.backend.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AccessControlService accessControlService;

    @Test
    void doesNotDuplicateActiveRole() {
        when(userRoleRepository.existsByUser_IdAndRole_CodeAndActiveTrue(7L, RoleCode.OPERATOR))
            .thenReturn(true);

        assertThat(accessControlService.ensureRole(7L, RoleCode.OPERATOR, 1L)).isFalse();
        verify(userRoleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void assignsRoleWithGrantingUser() {
        AppUser user = new AppUser();
        Role role = role(RoleCode.OPERATOR);
        when(userRoleRepository.existsByUser_IdAndRole_CodeAndActiveTrue(7L, RoleCode.OPERATOR))
            .thenReturn(false);
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));
        when(roleRepository.findByCode(RoleCode.OPERATOR)).thenReturn(Optional.of(role));

        assertThat(accessControlService.ensureRole(7L, RoleCode.OPERATOR, 1L)).isTrue();

        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getRole()).isSameAs(role);
        assertThat(captor.getValue().getGrantedByUserId()).isEqualTo(1L);
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    void returnsSortedActiveRoleCodes() {
        UserRole viewer = userRole(role(RoleCode.VIEWER));
        UserRole admin = userRole(role(RoleCode.ADMIN));
        when(userRoleRepository.findByUser_IdAndActiveTrue(7L)).thenReturn(List.of(viewer, admin));

        assertThat(accessControlService.getRoleCodes(7L)).containsExactly("ADMIN", "VIEWER");
    }

    @Test
    void rejectsRevokingMissingRole() {
        when(userRoleRepository.findByUser_IdAndRole_CodeAndActiveTrue(7L, RoleCode.ADMIN))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessControlService.revokeRole(7L, RoleCode.ADMIN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("нет активной роли");
    }

    private Role role(RoleCode code) {
        Role role = new Role();
        role.setCode(code);
        role.setNameRu(code.name());
        return role;
    }

    private UserRole userRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        return userRole;
    }
}
