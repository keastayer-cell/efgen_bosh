package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    @EntityGraph(attributePaths = "lines")
    Optional<WorkOrder> findByCarId(Long carId);
    @EntityGraph(attributePaths = {"lines", "partLines", "car"})
    List<WorkOrder> findAllByOrderByUpdatedAtDesc();
    void deleteByCar_Id(Long carId);
}
