package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.WorkCatalogItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkCatalogItemRepository extends JpaRepository<WorkCatalogItem, Long> {
    @EntityGraph(attributePaths = "category")
    List<WorkCatalogItem> findAllByActiveTrueOrderBySortOrderAscNameAsc();
}
