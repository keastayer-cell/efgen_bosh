package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.Part;

import java.time.LocalDate;

public record PartResponse(
    Long id, String legacyId, String name, String article, Long supplierId,
    LocalDate expectedDate, boolean received, LocalDate receivedAt,
    boolean overdue, Integer sortOrder
) {
    public static PartResponse from(Part part, LocalDate today) {
        boolean overdue = !part.isReceived() && part.getExpectedDate() != null
            && part.getExpectedDate().isBefore(today);
        return new PartResponse(part.getId(), part.getLegacyId(), part.getName(),
            part.getArticle(), part.getSupplierId(), part.getExpectedDate(),
            part.isReceived(), part.getReceivedAt(), overdue, part.getSortOrder());
    }
}
