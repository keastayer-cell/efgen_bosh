package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.RepairCaseStatusEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairCaseStatusRepository extends JpaRepository<RepairCaseStatusEntity, Integer> {
    List<RepairCaseStatusEntity> findAllByActiveTrueOrderBySortOrderAsc();
    Optional<RepairCaseStatusEntity> findByCodeAndActiveTrue(String code);
}
