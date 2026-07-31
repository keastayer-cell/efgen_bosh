package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_car_photo", schema = "work")
public class CarPhoto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "car_id", nullable = false) private Car car;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "repair_case_id") private RepairCase repairCase;
    @Column(name = "file_name", nullable = false, length = 255) private String fileName = "";
    @Column(name = "mime_type", nullable = false, length = 100) private String mimeType = "image/jpeg";
    @Column(name = "data_url", nullable = false, columnDefinition = "text") private String dataUrl;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    public Long getId() { return id; }
    public Car getCar() { return car; }
    public void setCar(Car value) { car = value; }
    public RepairCase getRepairCase() { return repairCase; }
    public void setRepairCase(RepairCase value) { repairCase = value; }
    public String getFileName() { return fileName; }
    public void setFileName(String value) { fileName = value == null ? "" : value; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String value) { mimeType = value == null ? "image/jpeg" : value; }
    public String getDataUrl() { return dataUrl; }
    public void setDataUrl(String value) { dataUrl = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
