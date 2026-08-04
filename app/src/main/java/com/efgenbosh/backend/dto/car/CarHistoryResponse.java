package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.CarHistory;
import java.time.OffsetDateTime;

public record CarHistoryResponse(Long id, Long carId, String eventType, String details, OffsetDateTime createdAt) {
    public static CarHistoryResponse from(CarHistory h) { return new CarHistoryResponse(h.getId(), h.getCar().getId(), h.getEventType(), h.getDetails(), h.getCreatedAt()); }
}
