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
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CarService {
    private final CarRepository cars;
    private final PartRepository parts;
    private final WorkOrderRepository workOrders;
    private final DefectAnalysisRepository defectAnalyses;

    public CarService(CarRepository cars, PartRepository parts, WorkOrderRepository workOrders, DefectAnalysisRepository defectAnalyses) {
        this.cars = cars;
        this.parts = parts;
        this.workOrders = workOrders;
        this.defectAnalyses = defectAnalyses;
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
            || value(car.getRegistrationNumber()).toLowerCase().contains(needle)
            || value(car.getVin()).toLowerCase().contains(needle)
            || value(car.getClaimNumber()).toLowerCase().contains(needle)).map(this::response).toList();
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
        return response(cars.save(car));
    }

    @Transactional
    public CarResponse update(Long id, CarRequest request) {
        Car car = car(id);
        apply(car, request);
        car.touch();
        return response(car);
    }

    @Transactional
    public CarResponse setDelivered(Long id, boolean delivered) {
        Car car = car(id);
        car.setDelivered(delivered);
        car.setDeliveredAt(delivered ? LocalDate.now() : null);
        car.touch();
        return response(car);
    }

    @Transactional
    public CarResponse setAccepted(Long id, boolean accepted) {
        Car car = car(id);
        car.setAcceptedAt(accepted ? LocalDate.now() : null);
        car.touch();
        return response(car);
    }

    @Transactional
    public void delete(Long id) {
        car(id);
        defectAnalyses.deleteByCar_Id(id);
        workOrders.deleteByCar_Id(id);
        cars.deleteById(id);
    }

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

    private CarResponse response(Car car) { return CarResponse.from(car, LocalDate.now()); }

    private void apply(Car car, CarRequest r) {
        car.setLegacyId(r.legacyId());
        car.setVehicleName(r.vehicleName().trim());
        car.setVehicleNameLatin(value(r.vehicleNameLatin()));
        car.setRegistrationNumber(LegacyBusinessRules.normalizeRegistrationNumber(r.registrationNumber()));
        car.setVin(LegacyBusinessRules.normalizeVin(r.vin()));
        car.setInsuredPerson(value(r.insuredPerson()));
        car.setClaimNumber(value(r.claimNumber()));
        car.setInsurerId(r.insurerId()); car.setContractorId(r.contractorId());
        car.setAcceptedAt(r.acceptedAt()); car.setStartedAt(r.startedAt());
        car.setAppointmentDate(r.appointmentDate()); car.setShiftId(r.shiftId());
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
