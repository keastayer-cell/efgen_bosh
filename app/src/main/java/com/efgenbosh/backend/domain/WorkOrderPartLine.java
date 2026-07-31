package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "w_work_order_part_line", schema = "work")
public class WorkOrderPartLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "work_order_id", nullable = false) private WorkOrder workOrder;
    @Column(name = "part_id") private Long partId;
    @Column(name = "name_snapshot", nullable = false) private String nameSnapshot;
    @Column(name = "article_snapshot", nullable = false) private String articleSnapshot = "";
    @Column(nullable = false, precision = 12, scale = 3) private BigDecimal quantity = BigDecimal.ONE;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal price = BigDecimal.ZERO;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    public Long getId() { return id; }
    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder value) { workOrder = value; }
    public Long getPartId() { return partId; }
    public void setPartId(Long value) { partId = value; }
    public String getNameSnapshot() { return nameSnapshot; }
    public void setNameSnapshot(String value) { nameSnapshot = value; }
    public String getArticleSnapshot() { return articleSnapshot; }
    public void setArticleSnapshot(String value) { articleSnapshot = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { quantity = value; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { sortOrder = value; }
    public BigDecimal total() { return quantity.multiply(price); }
}
