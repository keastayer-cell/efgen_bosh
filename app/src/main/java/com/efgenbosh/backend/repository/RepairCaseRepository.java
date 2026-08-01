package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.RepairCase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairCaseRepository extends JpaRepository<RepairCase, Long> {
    List<RepairCase> findAllByCar_IdOrderByCreatedAtDesc(Long carId);
    Optional<RepairCase> findFirstByCar_IdOrderByCreatedAtAsc(Long carId);
    List<RepairCase> findAllByOrderByCreatedAtDesc();
    @Query("select distinct r.car.id from RepairCase r where r.contractorId = :contractorId")
    List<Long> findCarIdsByContractorId(@Param("contractorId") Long contractorId);
}
