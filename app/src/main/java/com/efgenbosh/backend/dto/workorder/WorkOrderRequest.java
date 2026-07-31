package com.efgenbosh.backend.dto.workorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record WorkOrderRequest(
    LocalDate documentDate,
    @Size(max = 500) String customer,
    @Size(max = 32) String status,
    @Valid List<WorkOrderLineRequest> lines
) { }
