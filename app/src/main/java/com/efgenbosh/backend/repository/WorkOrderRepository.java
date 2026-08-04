package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    @EntityGraph(attributePaths = "lines")
    Optional<WorkOrder> findByCarId(Long carId);
    // Не подгружаем два List одним JOIN FETCH: Hibernate иначе выбрасывает MultipleBagFetchException.
    // partLines будут лениво дочитаны внутри транзакции сервиса.
    @EntityGraph(attributePaths = {"lines", "car", "repairCase"})
    Optional<WorkOrder> findByRepairCaseId(Long repairCaseId);
    @EntityGraph(attributePaths = {"lines", "partLines", "car"})
    List<WorkOrder> findAllByOrderByUpdatedAtDesc();
    void deleteByCar_Id(Long carId);
}
