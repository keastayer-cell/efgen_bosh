package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_contractor", schema = "work")
public class Contractor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String code;
    @Column(name = "short_name", nullable = false) private String shortName;
    @Column(name = "full_name", nullable = false, columnDefinition = "text") private String fullName;
    @Column(name = "signer_name", nullable = false) private String signerName = "";
    @Column(nullable = false, length = 20) private String inn = "";
    @Column(nullable = false, length = 20) private String ogrnip = "";
    @Column(nullable = false, columnDefinition = "text") private String address = "";
    @Column(name = "bank_name", nullable = false, columnDefinition = "text") private String bankName = "";
    @Column(name = "bank_inn", nullable = false) private String bankInn = "";
    @Column(name = "bank_kpp", nullable = false) private String bankKpp = "";
    @Column(nullable = false) private String bik = "";
    @Column(name = "correspondent_account", nullable = false) private String correspondentAccount = "";
    @Column(name = "settlement_account", nullable = false) private String settlementAccount = "";
    @Column(nullable = false) private boolean active = true;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    public Long getId() { return id; } public String getCode() { return code; } public void setCode(String v) { code = v; }
    public String getShortName() { return shortName; } public void setShortName(String v) { shortName = v; }
    public String getFullName() { return fullName; } public void setFullName(String v) { fullName = v; }
    public String getSignerName() { return signerName; } public void setSignerName(String v) { signerName = v; }
    public String getInn() { return inn; } public void setInn(String v) { inn = v; } public String getOgrnip() { return ogrnip; } public void setOgrnip(String v) { ogrnip = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; } public String getBankName() { return bankName; } public void setBankName(String v) { bankName = v; }
    public String getBankInn() { return bankInn; } public void setBankInn(String v) { bankInn = v; } public String getBankKpp() { return bankKpp; } public void setBankKpp(String v) { bankKpp = v; }
    public String getBik() { return bik; } public void setBik(String v) { bik = v; } public String getCorrespondentAccount() { return correspondentAccount; } public void setCorrespondentAccount(String v) { correspondentAccount = v; }
    public String getSettlementAccount() { return settlementAccount; } public void setSettlementAccount(String v) { settlementAccount = v; } public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public Integer getSortOrder() { return sortOrder; }
}
