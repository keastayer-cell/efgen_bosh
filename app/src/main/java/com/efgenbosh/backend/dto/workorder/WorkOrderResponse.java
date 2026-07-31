package com.efgenbosh.backend.dto.workorder;

import com.efgenbosh.backend.domain.WorkOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WorkOrderResponse(
    Long id, Long carId, String status, LocalDate documentDate, String customer,
    String orderNumber, String invoiceNumber, String actNumber,
    String claimNumber, String vehicleName, String registrationNumber, String vin,
    List<WorkOrderLineResponse> lines, List<WorkOrderPartLineResponse> partLines, BigDecimal total
) {
    public static WorkOrderResponse from(WorkOrder order) {
        return new WorkOrderResponse(order.getId(), order.getCar() == null ? null : order.getCar().getId(), order.getStatus(),
            order.getDocumentDate(), order.getCustomer(), order.getOrderNumber(), order.getInvoiceNumber(), order.getActNumber(), order.getClaimNumber(),
            order.getVehicleName(), order.getRegistrationNumber(), order.getVin(),
            order.getLines().stream().map(WorkOrderLineResponse::from).toList(),
            order.getPartLines().stream().map(WorkOrderPartLineResponse::from).toList(), order.total());
    }
}
