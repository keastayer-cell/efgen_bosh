package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.AppUser;
import com.efgenbosh.backend.domain.Role;
import com.efgenbosh.backend.domain.RoleCode;
import com.efgenbosh.backend.domain.UserRole;
import com.efgenbosh.backend.repository.AppUserRepository;
import com.efgenbosh.backend.repository.RoleRepository;
import com.efgenbosh.backend.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccessControlService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public AccessControlService(
        AppUserRepository appUserRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public void assignDefaultUserRole(Long userId) {
        ensureRole(userId, RoleCode.VIEWER, null);
    }

    @Transactional
    public boolean ensureRole(Long userId, RoleCode roleCode, Long grantedByUserId) {
        if (userRoleRepository.existsByUser_IdAndRole_CodeAndActiveTrue(userId, roleCode)) {
            return false;
        }

        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));
        Role role = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleCode));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setGrantedByUserId(grantedByUserId);
        userRole.setActive(true);
        userRoleRepository.save(userRole);
        return true;
    }

    @Transactional(readOnly = true)
    public List<String> getRoleCodes(Long userId) {
        return userRoleRepository.findByUser_IdAndActiveTrue(userId).stream()
            .map(userRole -> userRole.getRole().getCode().name())
            .sorted()
            .toList();
    }

    @Transactional
    public void revokeRole(Long userId, RoleCode roleCode) {
        UserRole userRole = userRoleRepository.findByUser_IdAndRole_CodeAndActiveTrue(userId, roleCode)
            .orElseThrow(() -> new IllegalArgumentException(
                "У пользователя нет активной роли " + roleCode + "."
            ));
        userRole.setActive(false);
        userRoleRepository.save(userRole);
    }
}
