package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.workorder.WorkOrderRequest;
import com.efgenbosh.backend.dto.workorder.WorkOrderResponse;
import com.efgenbosh.backend.security.AppUserPrincipal;
import com.efgenbosh.backend.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cars/{carId}/work-order")
public class WorkOrderController {
    private final WorkOrderService service;
    public WorkOrderController(WorkOrderService service) { this.service = service; }

    @GetMapping
    public WorkOrderResponse find(@PathVariable Long carId) { return service.findByCarId(carId); }

    @PutMapping
    public WorkOrderResponse save(@PathVariable Long carId, @Valid @RequestBody WorkOrderRequest request,
                                  Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        Long userId = principal instanceof AppUserPrincipal user ? user.getUserId() : null;
        return service.save(carId, request, userId);
    }
}
