package com.efgenbosh.backend.repository;
import com.efgenbosh.backend.domain.VehicleModel; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface VehicleModelRepository extends JpaRepository<VehicleModel,Long> { List<VehicleModel> findAllByMake_IdAndActiveTrueOrderByNameAsc(Long makeId); boolean existsByMake_IdAndNameIgnoreCase(Long makeId, String name); }
