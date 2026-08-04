package com.efgenbosh.backend.dto.workorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record WorkOrderRequest(
    LocalDate documentDate,
    @Size(max = 500) String customer,
    @Size(max = 64) String orderNumber,
    @Size(max = 64) String invoiceNumber,
    @Size(max = 64) String actNumber,
    @Size(max = 32) String status,
    @Valid List<WorkOrderLineRequest> lines,
    @Valid List<WorkOrderPartLineRequest> partLines
) { }
