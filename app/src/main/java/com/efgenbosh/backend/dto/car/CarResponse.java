package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.Car;
import com.efgenbosh.backend.service.LegacyBusinessRules;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record CarResponse(
    Long id, Long accountingNumber, String legacyId, String vehicleName,
    String vehicleNameLatin, String vehicleMake, String vehicleModel, String registrationNumber, String vin,
    String ownerName, String ownerPhone,
    String insuredPerson, String claimNumber, Long insurerId, Long contractorId,
    LocalDate acceptedAt, LocalDate startedAt, LocalDate appointmentDate,
    Long shiftId, String comment, String documentFolderUrl, boolean delivered,
    LocalDate deliveredAt, LegacyBusinessRules.CarStatus status,
    OffsetDateTime createdAt, OffsetDateTime updatedAt, List<PartResponse> parts
) {
    public static CarResponse from(Car car, LocalDate today) {
        List<PartResponse> parts = car.getParts().stream()
            .map(part -> PartResponse.from(part, today)).toList();
        var status = LegacyBusinessRules.carStatus(
            car.isDelivered(), parts.stream().map(p -> new LegacyBusinessRules.PartState(p.received())).toList());
        return new CarResponse(car.getId(), car.getAccountingNumber(), car.getLegacyId(),
            car.getVehicleName(), car.getVehicleNameLatin(), car.getVehicleMake(), car.getVehicleModel(), car.getRegistrationNumber(),
            car.getVin(), car.getOwnerName(), car.getOwnerPhone(), car.getInsuredPerson(), car.getClaimNumber(), car.getInsurerId(),
            car.getContractorId(), car.getAcceptedAt(), car.getStartedAt(),
            car.getAppointmentDate(), car.getShiftId(), car.getComment(),
            car.getDocumentFolderUrl(), car.isDelivered(), car.getDeliveredAt(), status,
            car.getCreatedAt(), car.getUpdatedAt(), parts);
    }
}
