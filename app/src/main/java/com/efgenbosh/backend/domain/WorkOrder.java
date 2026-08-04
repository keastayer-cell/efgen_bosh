package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "w_work_order", schema = "work")
public class WorkOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = true) @JoinColumn(name = "car_id", nullable = true, unique = true) private Car car;
    @OneToOne(fetch = FetchType.LAZY, optional = true) @JoinColumn(name = "repair_case_id", nullable = true, unique = true) private RepairCase repairCase;
    @Column(nullable = false, length = 32) private String status = "DRAFT";
    @Column(name = "document_date", nullable = false) private LocalDate documentDate = LocalDate.now();
    @Column(nullable = false, length = 500) private String customer = "";
    @Column(name = "order_number", nullable = false, length = 64) private String orderNumber = "";
    @Column(name = "invoice_number", nullable = false, length = 64) private String invoiceNumber = "";
    @Column(name = "act_number", nullable = false, length = 64) private String actNumber = "";
    @Column(name = "claim_number", nullable = false, length = 120) private String claimNumber = "";
    @Column(name = "vehicle_name", nullable = false) private String vehicleName = "";
    @Column(name = "registration_number", nullable = false, length = 32) private String registrationNumber = "";
    @Column(nullable = false, length = 32) private String vin = "";
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC") private List<WorkOrderLine> lines = new ArrayList<>();
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC") private List<WorkOrderPartLine> partLines = new ArrayList<>();

    public Long getId() { return id; }
    public Car getCar() { return car; }
    public void setCar(Car value) { car = value; }
    public RepairCase getRepairCase() { return repairCase; }
    public void setRepairCase(RepairCase value) { repairCase = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDate getDocumentDate() { return documentDate; }
    public void setDocumentDate(LocalDate value) { documentDate = value; }
    public String getCustomer() { return customer; }
    public void setCustomer(String value) { customer = value; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String value) { orderNumber = value; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String value) { invoiceNumber = value; }
    public String getActNumber() { return actNumber; }
    public void setActNumber(String value) { actNumber = value; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String value) { claimNumber = value; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String value) { vehicleName = value; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String value) { registrationNumber = value; }
    public String getVin() { return vin; }
    public void setVin(String value) { vin = value; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long value) { createdBy = value; }
    public List<WorkOrderLine> getLines() { return lines; }
    public void replaceLines(List<WorkOrderLine> values) { lines.clear(); values.forEach(this::addLine); }
    public void addLine(WorkOrderLine line) { lines.add(line); line.setWorkOrder(this); }
    public List<WorkOrderPartLine> getPartLines() { return partLines; }
    public void replacePartLines(List<WorkOrderPartLine> values) { partLines.clear(); values.forEach(this::addPartLine); }
    public void addPartLine(WorkOrderPartLine line) { partLines.add(line); line.setWorkOrder(this); }
    public BigDecimal total() { return lines.stream().map(WorkOrderLine::total).reduce(BigDecimal.ZERO, BigDecimal::add)
        .add(partLines.stream().map(WorkOrderPartLine::total).reduce(BigDecimal.ZERO, BigDecimal::add)); }
    public void touch() { updatedAt = OffsetDateTime.now(); }
}
