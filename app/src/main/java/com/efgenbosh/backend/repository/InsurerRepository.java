package com.efgenbosh.backend.repository;
import com.efgenbosh.backend.domain.Insurer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InsurerRepository extends JpaRepository<Insurer, Long> {
    List<Insurer> findAllByActiveTrueOrderBySortOrderAscNameAsc();
}
