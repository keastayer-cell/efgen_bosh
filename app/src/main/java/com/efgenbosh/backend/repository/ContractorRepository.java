package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.Contractor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
    List<Contractor> findAllByActiveTrueOrderBySortOrderAscShortNameAsc();
}
