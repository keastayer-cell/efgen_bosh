package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.RepairCase;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record RepairCaseResponse(Long id, Long carId, String caseNumber, String status, String insuredPerson,
    String claimNumber, Long insurerId, Long contractorId, Long shiftId, LocalDate acceptedAt,
    OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static RepairCaseResponse from(RepairCase item) { return new RepairCaseResponse(item.getId(), item.getCar().getId(), item.getCaseNumber(), item.getStatus(), item.getInsuredPerson(), item.getClaimNumber(), item.getInsurerId(), item.getContractorId(), item.getShiftId(), item.getAcceptedAt(), item.getCreatedAt(), item.getUpdatedAt()); }
}
