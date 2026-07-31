package com.efgenbosh.backend.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RepairCaseRequest(@NotBlank @Size(max = 64) String caseNumber, String status,
    @Size(max = 500) String insuredPerson, @Size(max = 120) String claimNumber,
    @NotNull Long insurerId, @NotNull Long contractorId, Long shiftId, LocalDate acceptedAt) {}
