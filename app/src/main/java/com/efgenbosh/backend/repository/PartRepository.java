package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findAllByCarIdOrderBySortOrderAscIdAsc(Long carId);
    List<Part> findAllByRepairCase_IdOrderBySortOrderAscIdAsc(Long caseId);
}
