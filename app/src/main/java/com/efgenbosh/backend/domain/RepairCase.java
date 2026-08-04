package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_repair_case", schema = "work")
public class RepairCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "car_id", nullable = false) private Car car;
    @Column(name = "case_number", nullable = false, length = 64) private String caseNumber = "";
    @Column(nullable = false, length = 32) private String status = "CREATED";
    @Column(name = "repair_type", nullable = false, length = 16) private String repairType = "INSURANCE";
    @Column(name = "insured_person", nullable = false, length = 500) private String insuredPerson = "";
    @Column(name = "claim_number", nullable = false, length = 120) private String claimNumber = "";
    @Column(nullable = false, length = 1000) private String comment = "";
    @Column(name = "appointment_date") private LocalDate appointmentDate;
    @Column(name = "appointment_time") private java.time.LocalTime appointmentTime;
    @Column(name = "started_at") private OffsetDateTime startedAt;
    @Column(name = "delivered_at") private OffsetDateTime deliveredAt;
    @Column(name = "received_by", nullable = false) private String receivedBy = "";
    @Column(name = "insurer_id") private Long insurerId;
    @Column(name = "contractor_id") private Long contractorId;
    @Column(name = "shift_id") private Long shiftId;
    @Column(name = "accepted_at") private LocalDate acceptedAt;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "updated_by") private Long updatedBy;
    public Long getId() { return id; }
    public Car getCar() { return car; } public void setCar(Car value) { car = value; }
    public String getCaseNumber() { return caseNumber; } public void setCaseNumber(String value) { caseNumber = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public String getRepairType() { return repairType; } public void setRepairType(String value) { repairType = value; }
    public String getInsuredPerson() { return insuredPerson; } public void setInsuredPerson(String value) { insuredPerson = value; }
    public String getClaimNumber() { return claimNumber; } public void setClaimNumber(String value) { claimNumber = value; }
    public String getComment() { return comment; } public void setComment(String value) { comment = value; }
    public LocalDate getAppointmentDate() { return appointmentDate; } public void setAppointmentDate(LocalDate value) { appointmentDate = value; }
    public java.time.LocalTime getAppointmentTime() { return appointmentTime; } public void setAppointmentTime(java.time.LocalTime value) { appointmentTime = value; }
    public OffsetDateTime getStartedAt() { return startedAt; } public void setStartedAt(OffsetDateTime value) { startedAt = value; }
    public OffsetDateTime getDeliveredAt() { return deliveredAt; } public void setDeliveredAt(OffsetDateTime value) { deliveredAt = value; }
    public String getReceivedBy() { return receivedBy; } public void setReceivedBy(String value) { receivedBy = value; }
    public Long getInsurerId() { return insurerId; } public void setInsurerId(Long value) { insurerId = value; }
    public Long getContractorId() { return contractorId; } public void setContractorId(Long value) { contractorId = value; }
    public Long getShiftId() { return shiftId; } public void setShiftId(Long value) { shiftId = value; }
    public LocalDate getAcceptedAt() { return acceptedAt; } public void setAcceptedAt(LocalDate value) { acceptedAt = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void touch() { updatedAt = OffsetDateTime.now(); }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long value) { createdBy = value; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long value) { updatedBy = value; }
}
