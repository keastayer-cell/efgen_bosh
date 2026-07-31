package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.Insurer;
import com.efgenbosh.backend.domain.Shift;
import com.efgenbosh.backend.domain.Supplier;
import com.efgenbosh.backend.dto.directory.DirectoryItemRequest;
import com.efgenbosh.backend.dto.directory.DirectoryItemResponse;
import com.efgenbosh.backend.repository.InsurerRepository;
import com.efgenbosh.backend.repository.ShiftRepository;
import com.efgenbosh.backend.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/directories")
public class DirectoryController {
    private final InsurerRepository insurers;
    private final SupplierRepository suppliers;
    private final ShiftRepository shifts;

    public DirectoryController(InsurerRepository insurers, SupplierRepository suppliers, ShiftRepository shifts) {
        this.insurers = insurers; this.suppliers = suppliers; this.shifts = shifts;
    }

    @GetMapping("/insurers")
    public List<DirectoryItemResponse> insurers() {
        return insurers.findAllByActiveTrueOrderBySortOrderAscNameAsc().stream()
            .map(item -> new DirectoryItemResponse(item.getId(), item.getName())).toList();
    }

    @PostMapping("/insurers")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectoryItemResponse createInsurer(@Valid @RequestBody DirectoryItemRequest request) {
        Insurer item = new Insurer(); item.setName(request.name().trim());
        item = insurers.save(item);
        return new DirectoryItemResponse(item.getId(), item.getName());
    }

    @GetMapping("/suppliers")
    public List<DirectoryItemResponse> suppliers() {
        return suppliers.findAllByActiveTrueOrderBySortOrderAscNameAsc().stream()
            .map(item -> new DirectoryItemResponse(item.getId(), item.getName())).toList();
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectoryItemResponse createSupplier(@Valid @RequestBody DirectoryItemRequest request) {
        Supplier item = new Supplier(); item.setName(request.name().trim());
        item = suppliers.save(item);
        return new DirectoryItemResponse(item.getId(), item.getName());
    }

    @GetMapping("/shifts")
    public List<DirectoryItemResponse> shifts() {
        return shifts.findAllByActiveTrueOrderBySortOrderAscNameAsc().stream()
            .map(item -> new DirectoryItemResponse(item.getId(), item.getName())).toList();
    }

    @PostMapping("/shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectoryItemResponse createShift(@Valid @RequestBody DirectoryItemRequest request) {
        Shift item = new Shift(); item.setName(request.name().trim());
        item = shifts.save(item);
        return new DirectoryItemResponse(item.getId(), item.getName());
    }
}
