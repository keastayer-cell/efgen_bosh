package com.efgenbosh.backend.repository;
import com.efgenbosh.backend.domain.VehicleMake; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface VehicleMakeRepository extends JpaRepository<VehicleMake,Long> { List<VehicleMake> findAllByActiveTrueOrderByNameAsc(); }
