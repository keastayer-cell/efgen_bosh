package com.efgenbosh.backend.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "w_car", schema = "work")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legacy_id", length = 255)
    private String legacyId;

    @Column(name = "accounting_number", nullable = false, unique = true)
    private Long accountingNumber;

    @Column(name = "registration_number", nullable = false, length = 32)
    private String registrationNumber = "";

    @Column(name = "vehicle_name", nullable = false, length = 255)
    private String vehicleName;

    @Column(name = "vehicle_name_latin", nullable = false, length = 255)
    private String vehicleNameLatin = "";

    @Column(name = "vehicle_make", nullable = false, length = 120)
    private String vehicleMake = "";

    @Column(name = "vehicle_model", nullable = false, length = 180)
    private String vehicleModel = "";

    @Column(nullable = false, length = 32)
    private String vin = "";

    @Column(name = "owner_name", nullable = false, length = 255)
    private String ownerName = "";

    @Column(name = "owner_phone", nullable = false, length = 64)
    private String ownerPhone = "";

    @Column(name = "insured_person", nullable = false, length = 500)
    private String insuredPerson = "";

    @Column(name = "claim_number", nullable = false, length = 120)
    private String claimNumber = "";

    @Column(name = "insurer_id")
    private Long insurerId;

    @Column(name = "contractor_id")
    private Long contractorId;

    @Column(name = "accepted_at")
    private LocalDate acceptedAt;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(nullable = false, columnDefinition = "text")
    private String comment = "";

    @Column(name = "document_folder_url", nullable = false, columnDefinition = "text")
    private String documentFolderUrl = "";

    @Column(nullable = false)
    private boolean delivered;

    @Column(name = "delivered_at")
    private LocalDate deliveredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Part> parts = new ArrayList<>();

    public Long getId() { return id; }
    public String getLegacyId() { return legacyId; }
    public void setLegacyId(String value) { legacyId = value; }
    public Long getAccountingNumber() { return accountingNumber; }
    public void setAccountingNumber(Long value) { accountingNumber = value; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String value) { registrationNumber = value; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String value) { vehicleName = value; }
    public String getVehicleNameLatin() { return vehicleNameLatin; }
    public void setVehicleNameLatin(String value) { vehicleNameLatin = value; }
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String value) { vehicleMake = value; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String value) { vehicleModel = value; }
    public String getVin() { return vin; }
    public void setVin(String value) { vin = value; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String value) { ownerName = value; }
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String value) { ownerPhone = value; }
    public String getInsuredPerson() { return insuredPerson; }
    public void setInsuredPerson(String value) { insuredPerson = value; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String value) { claimNumber = value; }
    public Long getInsurerId() { return insurerId; }
    public void setInsurerId(Long value) { insurerId = value; }
    public Long getContractorId() { return contractorId; }
    public void setContractorId(Long value) { contractorId = value; }
    public LocalDate getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDate value) { acceptedAt = value; }
    public LocalDate getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDate value) { startedAt = value; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate value) { appointmentDate = value; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long value) { shiftId = value; }
    public String getComment() { return comment; }
    public void setComment(String value) { comment = value; }
    public String getDocumentFolderUrl() { return documentFolderUrl; }
    public void setDocumentFolderUrl(String value) { documentFolderUrl = value; }
    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean value) { delivered = value; }
    public LocalDate getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDate value) { deliveredAt = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void touch() { updatedAt = OffsetDateTime.now(); }
    public List<Part> getParts() { return parts; }
    public void addPart(Part part) { parts.add(part); part.setCar(this); }
    public void removePart(Part part) { parts.remove(part); part.setCar(null); }
}
