package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.VehicleAlias;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleAliasRepository extends JpaRepository<VehicleAlias, Long> {
    List<VehicleAlias> findAllByActiveTrueOrderBySortOrderAscSourceNameAsc();
}
