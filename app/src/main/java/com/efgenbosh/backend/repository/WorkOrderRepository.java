package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    @EntityGraph(attributePaths = "lines")
    Optional<WorkOrder> findByCarId(Long carId);
}
