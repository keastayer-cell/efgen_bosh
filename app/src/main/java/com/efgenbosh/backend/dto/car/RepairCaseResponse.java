package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.RepairCase;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record RepairCaseResponse(Long id, Long carId, String caseNumber, String repairType, String status, String insuredPerson,
    String claimNumber, Long insurerId, Long contractorId, Long shiftId, LocalDate acceptedAt, String comment,
    LocalDate appointmentDate, java.time.LocalTime appointmentTime, OffsetDateTime startedAt, OffsetDateTime deliveredAt, String receivedBy,
    OffsetDateTime createdAt, OffsetDateTime updatedAt, Long createdBy, String createdByName) {
    public static RepairCaseResponse from(RepairCase item) { return from(item, null); }
    public static RepairCaseResponse from(RepairCase item, String creatorName) { return new RepairCaseResponse(item.getId(), item.getCar().getId(), item.getCaseNumber(), item.getRepairType(), item.getStatus(), item.getInsuredPerson(), item.getClaimNumber(), item.getInsurerId(), item.getContractorId(), item.getShiftId(), item.getAcceptedAt(), item.getComment(), item.getAppointmentDate(), item.getAppointmentTime(), item.getStartedAt(), item.getDeliveredAt(), item.getReceivedBy(), item.getCreatedAt(), item.getUpdatedAt(), item.getCreatedBy(), creatorName); }
}
