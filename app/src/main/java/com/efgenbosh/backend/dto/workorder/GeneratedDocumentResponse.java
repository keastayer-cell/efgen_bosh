package com.efgenbosh.backend.dto.workorder;

import com.efgenbosh.backend.domain.GeneratedDocument;
import java.time.OffsetDateTime;

public record GeneratedDocumentResponse(Long id, Long workOrderId, String documentType, String documentNumber, OffsetDateTime createdAt) {
    public static GeneratedDocumentResponse from(GeneratedDocument d) { return new GeneratedDocumentResponse(d.getId(), d.getWorkOrder().getId(), d.getDocumentType(), d.getDocumentNumber(), d.getCreatedAt()); }
}
