package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_counterparty", schema = "work")
public class Counterparty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 500) private String name;
    @Column(nullable = false, length = 20) private String inn = "";
    @Column(nullable = false, columnDefinition = "text") private String address = "";
    @Column(nullable = false, length = 40) private String phone = "";
    @Column(nullable = false, columnDefinition = "text") private String note = "";
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getInn() { return inn; }
    public void setInn(String value) { inn = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getNote() { return note; }
    public void setNote(String value) { note = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
}
