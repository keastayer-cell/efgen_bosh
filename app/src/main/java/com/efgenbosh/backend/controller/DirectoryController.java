package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.Insurer;
import com.efgenbosh.backend.domain.Shift;
import com.efgenbosh.backend.domain.Supplier;
import com.efgenbosh.backend.dto.directory.WorkCatalogItemResponse;
import com.efgenbosh.backend.dto.directory.WorkCatalogItemRequest;
import com.efgenbosh.backend.dto.directory.DirectoryItemRequest;
import com.efgenbosh.backend.dto.directory.DirectoryItemResponse;
import com.efgenbosh.backend.repository.InsurerRepository;
import com.efgenbosh.backend.repository.ShiftRepository;
import com.efgenbosh.backend.repository.SupplierRepository;
import com.efgenbosh.backend.repository.WorkCatalogItemRepository;
import com.efgenbosh.backend.repository.WorkCategoryRepository;
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
    private final WorkCatalogItemRepository workCatalog;
    private final WorkCategoryRepository workCategories;

    public DirectoryController(InsurerRepository insurers, SupplierRepository suppliers, ShiftRepository shifts,
                               WorkCatalogItemRepository workCatalog, WorkCategoryRepository workCategories) {
        this.insurers = insurers; this.suppliers = suppliers; this.shifts = shifts;
        this.workCatalog = workCatalog; this.workCategories = workCategories;
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

    @PutMapping("/insurers/{id}")
    public DirectoryItemResponse updateInsurer(@PathVariable Long id, @Valid @RequestBody DirectoryItemRequest request) {
        Insurer item = insurers.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Страховая не найдена."));
        item.setName(request.name().trim());
        return new DirectoryItemResponse(item.getId(), insurers.save(item).getName());
    }

    @DeleteMapping("/insurers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInsurer(@PathVariable Long id) { insurers.findById(id).ifPresent(item -> { item.setActive(false); insurers.save(item); }); }

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

    @PutMapping("/suppliers/{id}")
    public DirectoryItemResponse updateSupplier(@PathVariable Long id, @Valid @RequestBody DirectoryItemRequest request) {
        Supplier item = suppliers.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Поставщик не найден."));
        item.setName(request.name().trim());
        return new DirectoryItemResponse(item.getId(), suppliers.save(item).getName());
    }

    @DeleteMapping("/suppliers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(@PathVariable Long id) { suppliers.findById(id).ifPresent(item -> { item.setActive(false); suppliers.save(item); }); }

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

    @PutMapping("/shifts/{id}")
    public DirectoryItemResponse updateShift(@PathVariable Long id, @Valid @RequestBody DirectoryItemRequest request) {
        Shift item = shifts.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Смена не найдена."));
        item.setName(request.name().trim());
        return new DirectoryItemResponse(item.getId(), shifts.save(item).getName());
    }

    @DeleteMapping("/shifts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShift(@PathVariable Long id) { shifts.findById(id).ifPresent(item -> { item.setActive(false); shifts.save(item); }); }

    @GetMapping("/works")
    public List<WorkCatalogItemResponse> works() {
        return workCatalog.findAllByActiveTrueOrderBySortOrderAscNameAsc().stream()
            .map(item -> new WorkCatalogItemResponse(item.getId(), item.getCode(), item.getName(),
                item.getCategory().getName(), item.getDefaultUnit()))
            .toList();
    }

    @PostMapping("/works")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkCatalogItemResponse createWork(@Valid @RequestBody WorkCatalogItemRequest request) {
        String categoryName = request.categoryName().trim();
        var category = workCategories.findByNameIgnoreCase(categoryName).orElseGet(() -> {
            var created = new com.efgenbosh.backend.domain.WorkCategory();
            created.setName(categoryName);
            return workCategories.save(created);
        });
        var item = new com.efgenbosh.backend.domain.WorkCatalogItem();
        item.setCategory(category);
        item.setCode(request.code().trim());
        item.setName(request.name().trim());
        item.setDefaultUnit(request.defaultUnit().trim());
        item = workCatalog.save(item);
        return new WorkCatalogItemResponse(item.getId(), item.getCode(), item.getName(),
            item.getCategory().getName(), item.getDefaultUnit());
    }

    @DeleteMapping("/works/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWork(@PathVariable Long id) { workCatalog.findById(id).ifPresent(item -> { item.setActive(false); workCatalog.save(item); }); }
}
