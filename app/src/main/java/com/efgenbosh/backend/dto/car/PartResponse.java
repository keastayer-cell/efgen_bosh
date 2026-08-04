package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.Part;

import java.time.LocalDate;

public record PartResponse(
    Long id, String legacyId, String name, String article, Long supplierId, String catalogNumber, String manufacturer, java.math.BigDecimal quantity, LocalDate orderedAt,
    LocalDate expectedDate, boolean received, LocalDate receivedAt, boolean declined, String comment,
    boolean overdue, Integer sortOrder
) {
    public static PartResponse from(Part part, LocalDate today) {
        boolean overdue = !part.isReceived() && !part.isDeclined() && part.getExpectedDate() != null
            && part.getExpectedDate().isBefore(today);
        return new PartResponse(part.getId(), part.getLegacyId(), part.getName(),
            part.getArticle(), part.getSupplierId(), part.getCatalogNumber(), part.getManufacturer(), part.getQuantity(), part.getOrderedAt(), part.getExpectedDate(),
            part.isReceived(), part.getReceivedAt(), part.isDeclined(), part.getComment(), overdue, part.getSortOrder());
    }
}
