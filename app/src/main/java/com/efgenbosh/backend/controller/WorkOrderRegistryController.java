package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.workorder.WorkOrderRegistryResponse;
import com.efgenbosh.backend.repository.WorkOrderRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderRegistryController {
    private final WorkOrderRepository orders;

    public WorkOrderRegistryController(WorkOrderRepository orders) { this.orders = orders; }

    @GetMapping
    public List<WorkOrderRegistryResponse> list() {
        return orders.findAllByOrderByUpdatedAtDesc().stream().map(WorkOrderRegistryResponse::from).toList();
    }
}
