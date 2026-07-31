package com.efgenbosh.backend.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CarRequest(
    String legacyId,
    @NotBlank @Size(max = 255) String vehicleName,
    @Size(max = 255) String vehicleNameLatin,
    @Size(max = 32) String registrationNumber,
    @Size(max = 32) String vin,
    @Size(max = 500) String insuredPerson,
    @Size(max = 120) String claimNumber,
    Long insurerId,
    Long contractorId,
    LocalDate acceptedAt,
    LocalDate startedAt,
    LocalDate appointmentDate,
    Long shiftId,
    String comment,
    String documentFolderUrl
) {
}
