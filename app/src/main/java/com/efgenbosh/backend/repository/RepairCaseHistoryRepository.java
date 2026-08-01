package com.efgenbosh.backend.repository;
import com.efgenbosh.backend.domain.RepairCaseHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RepairCaseHistoryRepository extends JpaRepository<RepairCaseHistory,Long> { List<RepairCaseHistory> findAllByRepairCase_IdOrderByCreatedAtDesc(Long caseId); }
