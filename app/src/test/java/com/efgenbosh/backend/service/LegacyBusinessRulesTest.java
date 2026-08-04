package com.efgenbosh.backend.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.efgenbosh.backend.service.LegacyBusinessRules.CarStatus.DELIVERED;
import static com.efgenbosh.backend.service.LegacyBusinessRules.CarStatus.EMPTY;
import static com.efgenbosh.backend.service.LegacyBusinessRules.CarStatus.READY;
import static com.efgenbosh.backend.service.LegacyBusinessRules.CarStatus.WAITING;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyBusinessRulesTest {

    @Test
    void deliveredOverridesEveryPartState() {
        assertThat(
            LegacyBusinessRules.carStatus(
                true,
                List.of(new LegacyBusinessRules.PartState(false))
            )
        ).isEqualTo(DELIVERED);
    }

    @Test
    void carWithoutPartsIsEmpty() {
        assertThat(LegacyBusinessRules.carStatus(false, List.of()))
            .isEqualTo(EMPTY);
    }

    @Test
    void carIsReadyOnlyWhenEveryPartIsReceived() {
        assertThat(
            LegacyBusinessRules.carStatus(
                false,
                List.of(
                    new LegacyBusinessRules.PartState(true),
                    new LegacyBusinessRules.PartState(true)
                )
            )
        ).isEqualTo(READY);
        assertThat(
            LegacyBusinessRules.carStatus(
                false,
                List.of(
                    new LegacyBusinessRules.PartState(true),
                    new LegacyBusinessRules.PartState(false)
                )
            )
        ).isEqualTo(WAITING);
    }

    @Test
    void overdueIsSeparateFromCarStatusAndUsesStrictPastDate() {
        LocalDate today = LocalDate.of(2026, 7, 31);

        assertThat(
            LegacyBusinessRules.isOverdue(false, "2026-07-30", today)
        ).isTrue();
        assertThat(
            LegacyBusinessRules.isOverdue(false, "2026-07-31", today)
        ).isFalse();
        assertThat(
            LegacyBusinessRules.isOverdue(true, "2026-07-30", today)
        ).isFalse();
        assertThat(
            LegacyBusinessRules.isOverdue(false, "31.07.2026", today)
        ).isFalse();
    }

    @Test
    void nextAccountingNumberUsesMaximumNumericValue() {
        assertThat(
            LegacyBusinessRules.nextAccountingNumber(
                List.of("7", "invalid", "12", "3")
            )
        ).isEqualTo("13");
        assertThat(LegacyBusinessRules.nextAccountingNumber(List.of()))
            .isEqualTo("1");
    }

    @Test
    void registrationNumberAndVinLoseWhitespaceAndBecomeUppercase() {
        assertThat(
            LegacyBusinessRules.normalizeRegistrationNumber(" а 123 вс 77 ")
        ).isEqualTo("А123ВС77");
        assertThat(
            LegacyBusinessRules.normalizeVin(" wvw zz z1jz 3w 000001 ")
        ).isEqualTo("WVWZZZ1JZ3W000001");
    }

    @Test
    void lineTotalMatchesLegacyQuantityTimesPriceRule() {
        assertThat(
            LegacyBusinessRules.lineTotal(
                new BigDecimal("2.5"),
                new BigDecimal("1200")
            )
        ).isEqualByComparingTo("3000");
        assertThat(
            LegacyBusinessRules.lineTotal(BigDecimal.ONE, null)
        ).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
