package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
    List<Counterparty> findAllByActiveTrueOrderByNameAsc();
}
