package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.Role;
import com.efgenbosh.backend.domain.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(RoleCode code);
}
