package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_generated_document", schema = "work")
public class GeneratedDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "work_order_id", nullable = false) private WorkOrder workOrder;
    @Column(name = "document_type", nullable = false, length = 32) private String documentType;
    @Column(name = "document_number", nullable = false, length = 64) private String documentNumber = "";
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    public Long getId() { return id; } public WorkOrder getWorkOrder() { return workOrder; } public void setWorkOrder(WorkOrder v) { workOrder = v; }
    public String getDocumentType() { return documentType; } public void setDocumentType(String v) { documentType = v; }
    public String getDocumentNumber() { return documentNumber; } public void setDocumentNumber(String v) { documentNumber = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
