package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.workorder.WorkOrderRequest;
import com.efgenbosh.backend.dto.workorder.WorkOrderResponse;
import com.efgenbosh.backend.security.AppUserPrincipal;
import com.efgenbosh.backend.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/work-orders")
public class StandaloneWorkOrderController {
    private final WorkOrderService service;
    public StandaloneWorkOrderController(WorkOrderService service) { this.service = service; }
    @PostMapping("/standalone")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request, Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        Long userId = principal instanceof AppUserPrincipal user ? user.getUserId() : null;
        return service.createStandalone(request, userId);
    }
    @GetMapping("/{id}")
    public WorkOrderResponse find(@PathVariable Long id) { return service.findById(id); }
}
