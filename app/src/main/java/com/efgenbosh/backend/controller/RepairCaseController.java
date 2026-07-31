package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.Car;
import com.efgenbosh.backend.domain.RepairCase;
import com.efgenbosh.backend.dto.car.RepairCaseRequest;
import com.efgenbosh.backend.dto.car.RepairCaseResponse;
import com.efgenbosh.backend.repository.CarRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cars/{carId}/repair-cases")
public class RepairCaseController {
    private final RepairCaseRepository cases; private final CarRepository cars;
    public RepairCaseController(RepairCaseRepository cases, CarRepository cars) { this.cases = cases; this.cars = cars; }
    @GetMapping public List<RepairCaseResponse> list(@PathVariable Long carId) { ensureCar(carId); return cases.findAllByCar_IdOrderByCreatedAtDesc(carId).stream().map(RepairCaseResponse::from).toList(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public RepairCaseResponse create(@PathVariable Long carId, @Valid @RequestBody RepairCaseRequest request) {
        RepairCase item = new RepairCase(); item.setCar(ensureCar(carId)); apply(item, request); return RepairCaseResponse.from(cases.save(item));
    }
    @PutMapping("/{caseId}")
    public RepairCaseResponse update(@PathVariable Long carId, @PathVariable Long caseId, @Valid @RequestBody RepairCaseRequest request) {
        RepairCase item = cases.findById(caseId).filter(value -> value.getCar().getId().equals(carId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страховой случай не найден.")); apply(item, request); return RepairCaseResponse.from(cases.save(item));
    }
    @DeleteMapping("/{caseId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long carId, @PathVariable Long caseId) { RepairCase item = cases.findById(caseId).filter(value -> value.getCar().getId().equals(carId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страховой случай не найден.")); cases.delete(item); }
    private void apply(RepairCase item, RepairCaseRequest request) { item.setCaseNumber(request.caseNumber().trim()); item.setStatus(List.of("OPEN", "IN_REPAIR", "READY", "CLOSED").contains(String.valueOf(request.status()).toUpperCase()) ? request.status().toUpperCase() : "OPEN"); item.setInsuredPerson(value(request.insuredPerson())); item.setClaimNumber(value(request.claimNumber())); item.setInsurerId(request.insurerId()); item.setContractorId(request.contractorId()); item.setShiftId(request.shiftId()); item.setAcceptedAt(request.acceptedAt()); item.touch(); }
    private Car ensureCar(Long id) { return cars.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден.")); }
    private String value(String value) { return value == null ? "" : value.trim(); }
}
