package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.WorkCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkCategoryRepository extends JpaRepository<WorkCategory, Long> { }
