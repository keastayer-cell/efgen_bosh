package com.efgenbosh.backend.dto.workorder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WorkOrderLineRequest(
    String categoryName,
    @NotBlank String name,
    @NotBlank String unit,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    Long contractorId, String comment, Integer sortOrder
) { }
