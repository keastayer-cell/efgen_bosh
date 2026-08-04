package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.workorder.WorkOrderRequest;
import com.efgenbosh.backend.dto.workorder.WorkOrderResponse;
import com.efgenbosh.backend.security.AppUserPrincipal;
import com.efgenbosh.backend.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cars/{carId}/repair-cases/{caseId}/work-order")
public class RepairCaseWorkOrderController {
    private final WorkOrderService service;
    public RepairCaseWorkOrderController(WorkOrderService service) { this.service = service; }
    @GetMapping public WorkOrderResponse find(@PathVariable Long carId, @PathVariable Long caseId) { return service.findByCaseId(carId, caseId); }
    @PutMapping public WorkOrderResponse save(@PathVariable Long carId, @PathVariable Long caseId, @Valid @RequestBody WorkOrderRequest request, Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        Long userId = principal instanceof AppUserPrincipal user ? user.getUserId() : null;
        return service.saveCase(carId, caseId, request, userId);
    }
}
