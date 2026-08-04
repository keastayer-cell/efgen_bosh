package com.efgenbosh.backend.dto.car;
import com.efgenbosh.backend.domain.RepairCaseHistory;
import java.time.OffsetDateTime;
public record RepairCaseHistoryResponse(Long id, String previousStatus, String newStatus, String comment, OffsetDateTime createdAt, Long createdBy, String createdByName) { public static RepairCaseHistoryResponse from(RepairCaseHistory value){ return new RepairCaseHistoryResponse(value.getId(),value.getPreviousStatus(),value.getNewStatus(),value.getComment(),value.getCreatedAt(),value.getCreatedBy(),null); } }
