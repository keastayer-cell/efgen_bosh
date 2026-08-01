package com.efgenbosh.backend.domain;
import jakarta.persistence.*;
@Entity @Table(name="w_vehicle_model", schema="work")
public class VehicleModel { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="make_id") private VehicleMake make; @Column(nullable=false) private String name; @Column(nullable=false) private boolean active=true; public Long getId(){return id;} public String getName(){return name;} public void setName(String value){name=value;} public VehicleMake getMake(){return make;} public void setMake(VehicleMake value){make=value;} public boolean isActive(){return active;} public void setActive(boolean value){active=value;} }
