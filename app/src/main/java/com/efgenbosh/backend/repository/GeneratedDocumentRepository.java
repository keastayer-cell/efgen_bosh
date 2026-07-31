package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.GeneratedDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
    List<GeneratedDocument> findAllByWorkOrder_IdOrderByCreatedAtDesc(Long workOrderId);
}
