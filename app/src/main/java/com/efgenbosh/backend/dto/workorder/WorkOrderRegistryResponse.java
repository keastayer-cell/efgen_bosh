package com.efgenbosh.backend.dto.workorder;

import com.efgenbosh.backend.domain.WorkOrder;
import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkOrderRegistryResponse(Long id, Long carId, String orderNumber, String status,
                                        LocalDate documentDate, String customer, String vehicleName,
                                        String registrationNumber, BigDecimal total, String invoiceNumber,
                                        String actNumber) {
    public static WorkOrderRegistryResponse from(WorkOrder order) {
        return new WorkOrderRegistryResponse(order.getId(), order.getCar() == null ? null : order.getCar().getId(),
            order.getOrderNumber(), order.getStatus(), order.getDocumentDate(), order.getCustomer(),
            order.getVehicleName(), order.getRegistrationNumber(), order.total(), order.getInvoiceNumber(), order.getActNumber());
    }
}
