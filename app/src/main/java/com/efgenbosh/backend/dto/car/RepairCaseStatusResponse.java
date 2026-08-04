package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.RepairCaseStatus;

public record RepairCaseStatusResponse(int id, String code, String label) {
    public static RepairCaseStatusResponse from(RepairCaseStatus status) { return new RepairCaseStatusResponse(status.id(), status.code(), status.label()); }
}
