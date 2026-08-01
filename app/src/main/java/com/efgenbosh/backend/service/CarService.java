package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.Car;
import com.efgenbosh.backend.domain.Part;
import com.efgenbosh.backend.dto.car.CarRequest;
import com.efgenbosh.backend.dto.car.CarResponse;
import com.efgenbosh.backend.dto.car.PartRequest;
import com.efgenbosh.backend.dto.car.PartResponse;
import com.efgenbosh.backend.repository.CarRepository;
import com.efgenbosh.backend.repository.PartRepository;
import com.efgenbosh.backend.repository.WorkOrderRepository;
import com.efgenbosh.backend.repository.DefectAnalysisRepository;
import com.efgenbosh.backend.repository.CarHistoryRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import com.efgenbosh.backend.repository.RepairCaseStatusRepository;
import com.efgenbosh.backend.dto.car.RepairCaseRegistryResponse;
import com.efgenbosh.backend.dto.car.CarSearchSummary;
import com.efgenbosh.backend.domain.RepairCaseStatus;
import com.efgenbosh.backend.domain.CarHistory;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import com.efgenbosh.backend.dto.car.CarPageResponse;

@Service
public class CarService {
    private final CarRepository cars;
    private final PartRepository parts;
    private final WorkOrderRepository workOrders;
    private final DefectAnalysisRepository defectAnalyses;
    private final CarHistoryRepository history;
    private final RepairCaseRepository repairCases;
    private final RepairCaseStatusRepository statusDictionary;

    public CarService(CarRepository cars, PartRepository parts, WorkOrderRepository workOrders, DefectAnalysisRepository defectAnalyses, CarHistoryRepository history, RepairCaseRepository repairCases, RepairCaseStatusRepository statusDictionary) {
        this.cars = cars;
        this.parts = parts;
        this.workOrders = workOrders;
        this.defectAnalyses = defectAnalyses;
        this.history = history;
        this.repairCases = repairCases;
        this.statusDictionary = statusDictionary;
    }

    @Transactional
    public List<CarResponse> findAll() {
        return cars.findAllWithParts().stream().map(this::response).toList();
    }

    @Transactional
    public List<CarResponse> search(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        return cars.findAllWithParts().stream().filter(car -> needle.isBlank()
            || String.valueOf(car.getAccountingNumber()).contains(needle)
            || value(car.getVehicleName()).toLowerCase().contains(needle)
            || value(car.getVehicleMake()).toLowerCase().contains(needle)
            || value(car.getVehicleModel()).toLowerCase().contains(needle)
            || value(car.getRegistrationNumber()).toLowerCase().contains(needle)
            || value(car.getVin()).toLowerCase().contains(needle)
            || value(car.getOwnerPhone()).toLowerCase().contains(needle)
            || value(car.getOwnerName()).toLowerCase().contains(needle)
            || value(car.getClaimNumber()).toLowerCase().contains(needle)).map(this::response).toList();
    }

    @Transactional
    public CarPageResponse searchPage(String query, int page, int size, String status, Long insurerId, Long shiftId, Long contractorId, boolean overdue) {
        int safeSize = Math.max(1, Math.min(size, 100)); int safePage = Math.max(0, page);
        List<CarResponse> allCars = search(null);
        var summary = new CarSearchSummary(
            allCars.stream().filter(car -> currentCaseHas(car, RepairCaseStatus.CREATED, RepairCaseStatus.WAITING_PARTS, RepairCaseStatus.PARTS_RECEIVED, RepairCaseStatus.SCHEDULED, RepairCaseStatus.IN_REPAIR, RepairCaseStatus.READY)).count(),
            allCars.stream().filter(car -> currentCaseHas(car, RepairCaseStatus.WAITING_PARTS)).count(),
            allCars.stream().filter(car -> currentCaseHas(car, RepairCaseStatus.PARTS_RECEIVED)).count(),
            allCars.stream().filter(car -> currentCaseHas(car, RepairCaseStatus.DELIVERED)).count());
        List<CarResponse> all = allCars.stream()
            .filter(car -> matchesQuery(car, query))
            .filter(car -> matchesStatus(car, status))
            .filter(car -> insurerId == null || insurerId.equals(car.insurerId()))
            .filter(car -> shiftId == null || shiftId.equals(car.shiftId()))
            .filter(car -> contractorId == null || contractorId.equals(car.contractorId()))
            .filter(car -> !overdue || car.parts().stream().anyMatch(PartResponse::overdue))
            .toList();
        int from = Math.min(safePage * safeSize, all.size()); int to = Math.min(from + safeSize, all.size());
        long pages = all.isEmpty() ? 0 : (all.size() + safeSize - 1L) / safeSize;
        var statuses = statusDictionary.findAllByActiveTrueOrderBySortOrderAsc().stream().map(item -> new com.efgenbosh.backend.dto.car.RepairCaseStatusResponse(item.getId(), item.getCode(), item.getLabel())).toList();
        return new CarPageResponse(all.subList(from, to), safePage, safeSize, pages, all.size(), summary, statuses);
    }

    private boolean matchesStatus(CarResponse car, String requested) {
        if (requested == null || requested.isBlank() || "all".equalsIgnoreCase(requested)) return true;
        return switch (requested.toLowerCase()) {
            case "active" -> currentCaseHas(car, RepairCaseStatus.CREATED, RepairCaseStatus.WAITING_PARTS, RepairCaseStatus.PARTS_RECEIVED, RepairCaseStatus.SCHEDULED, RepairCaseStatus.IN_REPAIR, RepairCaseStatus.READY);
            case "waiting" -> currentCaseHas(car, RepairCaseStatus.WAITING_PARTS);
            case "ready" -> currentCaseHas(car, RepairCaseStatus.PARTS_RECEIVED);
            case "delivered" -> currentCaseHas(car, RepairCaseStatus.DELIVERED);
            case "waiting_parts", "parts_received" -> car.repairCases().stream().anyMatch(item -> requested.equalsIgnoreCase(item.status()));
            default -> car.repairCases().stream().anyMatch(item -> requested.equalsIgnoreCase(item.status()));
        };
    }

    private boolean currentCaseHas(CarResponse car, RepairCaseStatus... statuses) {
        return car.repairCases().stream().findFirst().map(item -> java.util.Arrays.stream(statuses).anyMatch(status -> status.code().equals(item.status()))).orElse(false);
    }

    private boolean matchesQuery(CarResponse car, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        return needle.isBlank()
            || String.valueOf(car.accountingNumber()).contains(needle)
            || value(car.vehicleName()).toLowerCase().contains(needle)
            || value(car.vehicleMake()).toLowerCase().contains(needle)
            || value(car.vehicleModel()).toLowerCase().contains(needle)
            || value(car.registrationNumber()).toLowerCase().contains(needle)
            || value(car.vin()).toLowerCase().contains(needle)
            || value(car.ownerPhone()).toLowerCase().contains(needle)
            || value(car.ownerName()).toLowerCase().contains(needle)
            || value(car.claimNumber()).toLowerCase().contains(needle)
            || car.repairCases().stream().anyMatch(item -> value(item.caseNumber()).toLowerCase().contains(needle) || value(item.claimNumber()).toLowerCase().contains(needle));
    }

    @Transactional
    public CarResponse findById(Long id) {
        return response(car(id));
    }

    @Transactional
    public CarResponse create(CarRequest request) {
        Car car = new Car();
        car.setAccountingNumber(cars.findMaximumAccountingNumber() + 1);
        apply(car, request);
        Car saved = cars.save(car); record(saved, "CAR_CREATED", "Автомобиль создан"); return response(saved);
    }

    @Transactional
    public CarResponse update(Long id, CarRequest request) {
        Car car = car(id);
        apply(car, request);
        car.touch();
        record(car, "CAR_UPDATED", "Карточка автомобиля изменена");
        return response(car);
    }

    @Transactional
    public CarResponse setDelivered(Long id, boolean delivered) {
        Car car = car(id);
        car.setDelivered(delivered);
        car.setDeliveredAt(delivered ? LocalDate.now() : null);
        car.touch();
        record(car, delivered ? "CAR_DELIVERED" : "CAR_DELIVERY_CANCELLED", delivered ? "Автомобиль выдан" : "Выдача отменена");
        return response(car);
    }

    @Transactional
    public CarResponse setAccepted(Long id, boolean accepted) {
        Car car = car(id);
        car.setAcceptedAt(accepted ? LocalDate.now() : null);
        car.touch();
        record(car, accepted ? "CAR_ACCEPTED" : "CAR_ACCEPTANCE_CANCELLED", accepted ? "Автомобиль принят" : "Приёмка отменена");
        return response(car);
    }

    @Transactional
    public void delete(Long id) {
        car(id);
        defectAnalyses.deleteByCar_Id(id);
        workOrders.deleteByCar_Id(id);
        cars.deleteById(id);
    }

    private void record(Car car, String type, String details) { CarHistory event = new CarHistory(); event.setCar(car); event.setEventType(type); event.setDetails(details); history.save(event); }

    @Transactional
    public PartResponse addPart(Long carId, PartRequest request) {
        Car car = car(carId);
        Part part = new Part();
        apply(part, request);
        part.touch();
        car.addPart(part);
        parts.save(part);
        return PartResponse.from(part, LocalDate.now());
    }

    @Transactional
    public PartResponse updatePart(Long carId, Long partId, PartRequest request) {
        Part part = part(partId);
        if (part.getCar() == null || !part.getCar().getId().equals(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запчасть не найдена.");
        }
        apply(part, request);
        return PartResponse.from(part, LocalDate.now());
    }

    @Transactional
    public PartResponse setPartReceived(Long carId, Long partId, boolean received) {
        Part part = part(partId);
        if (part.getCar() == null || !part.getCar().getId().equals(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запчасть не найдена.");
        }
        part.setReceived(received);
        part.setReceivedAt(received ? LocalDate.now() : null);
        part.touch();
        return PartResponse.from(part, LocalDate.now());
    }

    @Transactional
    public void deletePart(Long carId, Long partId) {
        Part part = part(partId);
        if (part.getCar() == null || !part.getCar().getId().equals(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запчасть не найдена.");
        }
        parts.delete(part);
    }

    private Car car(Long id) {
        return cars.findByIdWithParts(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден."));
    }

    private Part part(Long id) {
        return parts.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Запчасть не найдена."));
    }

    private CarResponse response(Car car) {
        var registryCases = repairCases.findAllByCar_IdOrderByCreatedAtDesc(car.getId()).stream().map(item -> {
            var caseParts = parts.findAllByRepairCase_IdOrderBySortOrderAscIdAsc(item.getId());
            return RepairCaseRegistryResponse.from(item, caseParts.size(), (int) caseParts.stream().filter(Part::isReceived).count());
        }).toList();
        return CarResponse.from(car, LocalDate.now(), registryCases);
    }

    private void apply(Car car, CarRequest r) {
        car.setLegacyId(r.legacyId());
        car.setVehicleMake(r.vehicleMake().trim());
        car.setVehicleModel(r.vehicleModel().trim());
        car.setVehicleName(car.getVehicleMake() + " " + car.getVehicleModel());
        car.setVehicleNameLatin(car.getVehicleName());
        car.setRegistrationNumber(LegacyBusinessRules.normalizeRegistrationNumber(r.registrationNumber()));
        car.setVin(LegacyBusinessRules.normalizeVin(r.vin()));
        car.setOwnerName(value(r.ownerName()));
        car.setOwnerPhone(value(r.ownerPhone()));
        car.setComment(value(r.comment())); car.setDocumentFolderUrl(value(r.documentFolderUrl()));
    }

    private void apply(Part part, PartRequest r) {
        part.setLegacyId(r.legacyId()); part.setName(r.name().trim());
        part.setArticle(value(r.article())); part.setSupplierId(r.supplierId());
        part.setExpectedDate(r.expectedDate());
        part.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());
    }

    private String value(String value) { return value == null ? "" : value.trim(); }
}
