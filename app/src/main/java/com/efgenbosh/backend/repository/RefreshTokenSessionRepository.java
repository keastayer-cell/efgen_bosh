package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.RefreshTokenSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenSession> findLockedByTokenHash(String tokenHash);
}
