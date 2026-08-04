package com.efgenbosh.backend.dto.workorder;

import com.efgenbosh.backend.domain.WorkOrderPartLine;
import java.math.BigDecimal;

public record WorkOrderPartLineResponse(
    Long id, Long partId, String name, String article,
    BigDecimal quantity, BigDecimal price, BigDecimal total, Integer sortOrder
) {
    public static WorkOrderPartLineResponse from(WorkOrderPartLine line) {
        return new WorkOrderPartLineResponse(line.getId(), line.getPartId(), line.getNameSnapshot(),
            line.getArticleSnapshot(), line.getQuantity(), line.getPrice(), line.total(), line.getSortOrder());
    }
}
