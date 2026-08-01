package com.efgenbosh.backend.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PartRequest(
    String legacyId,
    @NotBlank @Size(max = 500) String name,
    @Size(max = 255) String article,
    Long supplierId, String catalogNumber, String manufacturer, java.math.BigDecimal quantity,
    LocalDate orderedAt, LocalDate expectedDate, String comment, boolean received,
    Integer sortOrder
) {
}
