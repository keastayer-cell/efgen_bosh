package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.DefectAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefectAnalysisRepository extends JpaRepository<DefectAnalysis, Long> {
    Optional<DefectAnalysis> findByCar_Id(Long carId);
    void deleteByCar_Id(Long carId);
}
