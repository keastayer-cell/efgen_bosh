package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_car_history", schema = "work")
public class CarHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "car_id", nullable = false) private Car car;
    @Column(name = "event_type", nullable = false, length = 64) private String eventType;
    @Column(nullable = false, columnDefinition = "text") private String details = "";
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    public Long getId() { return id; } public Car getCar() { return car; } public void setCar(Car v) { car = v; }
    public String getEventType() { return eventType; } public void setEventType(String v) { eventType = v; }
    public String getDetails() { return details; } public void setDetails(String v) { details = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
