package com.efgenbosh.backend.repository;
import com.efgenbosh.backend.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findAllByActiveTrueOrderBySortOrderAscNameAsc();
}
