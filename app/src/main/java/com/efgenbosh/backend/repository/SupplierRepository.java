package com.efgenbosh.backend.repository;
import com.efgenbosh.backend.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findAllByActiveTrueOrderBySortOrderAscNameAsc();
}
