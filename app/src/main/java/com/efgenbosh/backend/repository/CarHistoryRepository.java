package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.CarHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarHistoryRepository extends JpaRepository<CarHistory, Long> {
    List<CarHistory> findAllByCar_IdOrderByCreatedAtDesc(Long carId);
}
