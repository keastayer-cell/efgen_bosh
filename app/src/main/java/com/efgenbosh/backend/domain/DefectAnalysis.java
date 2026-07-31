package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_defect_analysis", schema = "work")
public class DefectAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "car_id", nullable = false, unique = true) private Car car;
    @Column(nullable = false, length = 32) private String status = "DRAFT";
    @Column(nullable = false, columnDefinition = "text") private String findings = "";
    @Column(nullable = false, columnDefinition = "text") private String recommendations = "";
    @Column(name = "photos_json", nullable = false, columnDefinition = "text") private String photosJson = "[]";
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    public Long getId() { return id; }
    public Car getCar() { return car; }
    public void setCar(Car value) { car = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getFindings() { return findings; }
    public void setFindings(String value) { findings = value; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String value) { recommendations = value; }
    public String getPhotosJson() { return photosJson; }
    public void setPhotosJson(String value) { photosJson = value; }
    public void touch() { updatedAt = OffsetDateTime.now(); }
}
