package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.Car;
import com.efgenbosh.backend.domain.Part;
import com.efgenbosh.backend.dto.car.CarResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CarResponseTest {

    @Test
    void computesLegacyStatusAndOverduePartFromCurrentState() {
        Car car = new Car();
        car.setAccountingNumber(1L);
        car.setVehicleName("Lada");

        Part overdue = new Part();
        overdue.setName("Фара");
        overdue.setExpectedDate(LocalDate.of(2026, 7, 30));
        car.addPart(overdue);

        CarResponse response = CarResponse.from(car, LocalDate.of(2026, 7, 31));

        assertThat(response.status()).isEqualTo(LegacyBusinessRules.CarStatus.WAITING);
        assertThat(response.parts()).singleElement().satisfies(part -> {
            assertThat(part.overdue()).isTrue();
            assertThat(part.received()).isFalse();
        });
    }

    @Test
    void deliveredStatusOverridesPartState() {
        Car car = new Car();
        car.setVehicleName("Lada");
        car.setDelivered(true);
        car.setDeliveredAt(LocalDate.of(2026, 7, 31));

        assertThat(CarResponse.from(car, LocalDate.of(2026, 7, 31)).status())
            .isEqualTo(LegacyBusinessRules.CarStatus.DELIVERED);
    }
}
