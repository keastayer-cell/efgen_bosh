package com.efgenbosh.backend.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CarRequest(
    String legacyId,
    @NotBlank @Size(max = 120) String vehicleMake,
    @NotBlank @Size(max = 180) String vehicleModel,
    @NotBlank @Size(max = 32) String registrationNumber,
    @NotBlank @Size(max = 32) String vin,
    @NotBlank @Size(max = 255) String ownerName,
    @NotBlank @Size(max = 64) String ownerPhone,
    String comment,
    String documentFolderUrl
) {
}
