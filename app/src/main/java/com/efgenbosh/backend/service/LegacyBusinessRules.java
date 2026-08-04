package com.efgenbosh.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

public final class LegacyBusinessRules {

    private static final DateTimeFormatter ISO_DATE =
        DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT);

    private LegacyBusinessRules() {
    }

    public static CarStatus carStatus(
        boolean delivered,
        List<PartState> parts
    ) {
        if (delivered) {
            return CarStatus.DELIVERED;
        }
        if (parts == null || parts.isEmpty()) {
            return CarStatus.EMPTY;
        }
        if (parts.stream().allMatch(PartState::received)) {
            return CarStatus.READY;
        }
        return CarStatus.WAITING;
    }

    public static boolean isOverdue(
        boolean received,
        String expectedDate,
        LocalDate today
    ) {
        if (received || today == null) {
            return false;
        }
        LocalDate parsedDate = parseIsoDate(expectedDate);
        return parsedDate != null && parsedDate.isBefore(today);
    }

    public static String nextAccountingNumber(List<String> existingNumbers) {
        long maximum = 0;
        boolean found = false;
        if (existingNumbers != null) {
            for (String value : existingNumbers) {
                Long number = parseWholeNumber(value);
                if (number != null) {
                    maximum = found ? Math.max(maximum, number) : number;
                    found = true;
                }
            }
        }
        return String.valueOf(found ? maximum + 1 : 1);
    }

    public static String normalizeRegistrationNumber(String value) {
        return normalizeWithoutWhitespace(value);
    }

    public static String normalizeVin(String value) {
        return normalizeWithoutWhitespace(value);
    }

    public static BigDecimal lineTotal(BigDecimal quantity, BigDecimal price) {
        BigDecimal safeQuantity = quantity == null ? BigDecimal.ZERO : quantity;
        BigDecimal safePrice = price == null ? BigDecimal.ZERO : price;
        return safeQuantity.multiply(safePrice);
    }

    private static String normalizeWithoutWhitespace(String value) {
        return String.valueOf(value == null ? "" : value)
            .replaceAll("\\s+", "")
            .trim()
            .toUpperCase(Locale.ROOT);
    }

    private static LocalDate parseIsoDate(String value) {
        if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return null;
        }
        try {
            return LocalDate.parse(value, ISO_DATE);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private static Long parseWholeNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public enum CarStatus {
        DELIVERED,
        EMPTY,
        READY,
        WAITING
    }

    public record PartState(boolean received) {
    }
}
