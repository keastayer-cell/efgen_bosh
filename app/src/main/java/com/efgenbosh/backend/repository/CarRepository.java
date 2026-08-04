package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    @Query("select coalesce(max(c.accountingNumber), 0) from Car c")
    long findMaximumAccountingNumber();

    @Query("select distinct c from Car c left join fetch c.parts order by c.accountingNumber")
    List<Car> findAllWithParts();

    @Query("select distinct c from Car c left join fetch c.parts where c.id = :id")
    Optional<Car> findByIdWithParts(Long id);
}
