package com.efgenbosh.backend.dto.car;
import com.efgenbosh.backend.domain.RepairCase;
import java.time.OffsetDateTime;
public record RepairCaseRegistryResponse(Long id, Long carId, Long accountingNumber, String vehicleMake, String vehicleModel, String registrationNumber, String vin, String ownerName, String ownerPhone, String caseNumber, String status, String claimNumber, Long insurerId, OffsetDateTime createdAt, Long createdBy, OffsetDateTime updatedAt, Long updatedBy) {
    public static RepairCaseRegistryResponse from(RepairCase item) { var car = item.getCar(); return new RepairCaseRegistryResponse(item.getId(), car.getId(), car.getAccountingNumber(), car.getVehicleMake(), car.getVehicleModel(), car.getRegistrationNumber(), car.getVin(), car.getOwnerName(), car.getOwnerPhone(), item.getCaseNumber(), item.getStatus(), item.getClaimNumber(), item.getInsurerId(), item.getCreatedAt(), item.getCreatedBy(), item.getUpdatedAt(), item.getUpdatedBy()); }
}
