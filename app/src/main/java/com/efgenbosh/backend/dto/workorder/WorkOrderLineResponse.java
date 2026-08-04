package com.efgenbosh.backend.dto.workorder;

import com.efgenbosh.backend.domain.WorkOrderLine;
import java.math.BigDecimal;

public record WorkOrderLineResponse(
    Long id, String categoryName, String name, String unit,
    BigDecimal quantity, BigDecimal price, BigDecimal total, Integer sortOrder, Long contractorId, String comment
) {
    public static WorkOrderLineResponse from(WorkOrderLine line) {
        return new WorkOrderLineResponse(line.getId(), line.getCategoryNameSnapshot(),
            line.getNameSnapshot(), line.getUnit(), line.getQuantity(), line.getPrice(),
            line.total(), line.getSortOrder(), line.getContractorId(), line.getComment());
    }
}
