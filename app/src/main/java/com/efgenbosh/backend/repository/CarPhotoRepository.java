package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.CarPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarPhotoRepository extends JpaRepository<CarPhoto, Long> {
    List<CarPhoto> findAllByCar_IdOrderByCreatedAtDesc(Long carId);
    void deleteByCar_Id(Long carId);
}
