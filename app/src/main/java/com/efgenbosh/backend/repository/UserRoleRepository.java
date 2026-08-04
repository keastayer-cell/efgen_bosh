package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.RoleCode;
import com.efgenbosh.backend.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser_IdAndActiveTrue(Long userId);

    boolean existsByUser_IdAndRole_CodeAndActiveTrue(Long userId, RoleCode roleCode);

    Optional<UserRole> findByUser_IdAndRole_CodeAndActiveTrue(Long userId, RoleCode roleCode);
}
