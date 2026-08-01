package com.efgenbosh.backend.domain;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity @Table(name="w_repair_case_history", schema="work")
public class RepairCaseHistory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="repair_case_id") private RepairCase repairCase;
    @Column(name="previous_status") private String previousStatus;
    @Column(name="new_status", nullable=false) private String newStatus;
    @Column(nullable=false, columnDefinition="text") private String comment="";
    @Column(name="created_at", nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now();
    @Column(name="created_by") private Long createdBy;
    public Long getId(){return id;} public RepairCase getRepairCase(){return repairCase;} public void setRepairCase(RepairCase value){repairCase=value;}
    public String getPreviousStatus(){return previousStatus;} public void setPreviousStatus(String value){previousStatus=value;}
    public String getNewStatus(){return newStatus;} public void setNewStatus(String value){newStatus=value;}
    public String getComment(){return comment;} public void setComment(String value){comment=value==null?"":value;}
    public OffsetDateTime getCreatedAt(){return createdAt;} public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long value){createdBy=value;}
}
