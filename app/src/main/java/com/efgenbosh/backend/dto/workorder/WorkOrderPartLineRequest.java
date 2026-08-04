package com.efgenbosh.backend.dto.workorder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WorkOrderPartLineRequest(
    Long partId,
    @NotBlank String name,
    String article,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    Integer sortOrder
) { }
