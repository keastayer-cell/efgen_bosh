package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.Car;
import com.efgenbosh.backend.domain.RepairCase;
import com.efgenbosh.backend.dto.car.RepairCaseRequest;
import com.efgenbosh.backend.dto.car.RepairCaseResponse;
import com.efgenbosh.backend.dto.car.RepairCaseActionRequest;
import com.efgenbosh.backend.repository.CarRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import com.efgenbosh.backend.repository.RepairCaseHistoryRepository;
import com.efgenbosh.backend.repository.PartRepository;
import com.efgenbosh.backend.repository.WorkOrderRepository;
import com.efgenbosh.backend.repository.ContractorRepository;
import com.efgenbosh.backend.repository.AppUserRepository;
import com.efgenbosh.backend.domain.RepairCaseHistory;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import com.efgenbosh.backend.security.AppUserPrincipal;

@RestController
@RequestMapping("/api/v1/cars/{carId}/repair-cases")
public class RepairCaseController {
    private final RepairCaseRepository cases; private final CarRepository cars; private final RepairCaseHistoryRepository history; private final PartRepository parts; private final WorkOrderRepository workOrders; private final ContractorRepository contractors; private final AppUserRepository users;
    public RepairCaseController(RepairCaseRepository cases, CarRepository cars, RepairCaseHistoryRepository history, PartRepository parts, WorkOrderRepository workOrders, ContractorRepository contractors, AppUserRepository users) { this.cases = cases; this.cars = cars; this.history = history; this.parts = parts; this.workOrders = workOrders; this.contractors = contractors; this.users = users; }
    @GetMapping public List<RepairCaseResponse> list(@PathVariable Long carId) { ensureCar(carId); return cases.findAllByCar_IdOrderByCreatedAtDesc(carId).stream().map(this::response).toList(); }
    @GetMapping("/{caseId}")
    public RepairCaseResponse get(@PathVariable Long carId, @PathVariable Long caseId) {
        return response(findCase(carId, caseId));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public RepairCaseResponse create(@PathVariable Long carId, @Valid @RequestBody RepairCaseRequest request, Authentication authentication) {
        RepairCase item = new RepairCase(); item.setCar(ensureCar(carId)); apply(item, request); item.setRepairType(validType(request.repairType())); if ("INSURANCE".equals(item.getRepairType()) && request.insurerId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Для страхового случая выберите страховую компанию."); item.setStatus("CREATED"); item.setCreatedBy(userId(authentication)); item.setUpdatedBy(userId(authentication)); RepairCase saved=cases.save(item); record(saved,null,"CREATED","Обращение создано",userId(authentication)); return response(saved);
    }
    @PutMapping("/{caseId}")
    public RepairCaseResponse update(@PathVariable Long carId, @PathVariable Long caseId, @Valid @RequestBody RepairCaseRequest request, Authentication authentication) {
        RepairCase item = findCase(carId, caseId); ensureEditable(item); String previous=item.getStatus(); Long previousContractor = item.getContractorId(); apply(item, request); item.setUpdatedBy(userId(authentication)); RepairCase saved=cases.save(item); if(!previous.equals(saved.getStatus())) record(saved,previous,saved.getStatus(),"Статус обращения изменён",userId(authentication)); if(!java.util.Objects.equals(previousContractor, saved.getContractorId())) record(saved, saved.getStatus(), saved.getStatus(), contractorAction(saved), userId(authentication)); return response(saved);
    }
    @DeleteMapping("/{caseId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long carId, @PathVariable Long caseId) { RepairCase item = cases.findById(caseId).filter(value -> value.getCar().getId().equals(carId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страховой случай не найден.")); ensureEditable(item); cases.delete(item); }
    private void apply(RepairCase item, RepairCaseRequest request) { item.setCaseNumber(request.caseNumber().trim()); if (request.repairType() != null) item.setRepairType(validType(request.repairType())); if (request.status() != null && List.of("CREATED", "WAITING_PARTS", "PARTS_RECEIVED", "SCHEDULED", "IN_REPAIR", "READY", "DELIVERED", "CLOSED").contains(request.status().toUpperCase())) item.setStatus(request.status().toUpperCase()); item.setInsuredPerson(value(request.insuredPerson())); item.setClaimNumber(value(request.claimNumber())); item.setInsurerId(request.insurerId()); item.setContractorId(request.contractorId()); item.setShiftId(request.shiftId()); item.setAcceptedAt(request.acceptedAt()); item.setComment(value(request.comment())); item.setAppointmentDate(request.appointmentDate()); item.setAppointmentTime(request.appointmentTime()); item.setReceivedBy(value(request.receivedBy())); item.touch(); }
    private String validType(String value){ return "REPAIR".equalsIgnoreCase(value) ? "REPAIR" : "INSURANCE"; }
    @PostMapping("/{caseId}/actions/{action}")
    public RepairCaseResponse action(@PathVariable Long carId, @PathVariable Long caseId, @PathVariable String action, @RequestBody(required = false) RepairCaseActionRequest request, Authentication authentication) {
        RepairCase item = cases.findById(caseId).filter(value -> value.getCar().getId().equals(carId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страховой случай не найден."));
        String required = switch (action.toUpperCase()) { case "ORDER_PARTS" -> "CREATED"; case "SCHEDULE_REPAIR" -> "PARTS_RECEIVED"; case "START_REPAIR" -> "SCHEDULED"; case "FINISH_REPAIR" -> "IN_REPAIR"; case "DELIVER" -> "READY"; case "CLOSE" -> "DELIVERED"; default -> null; };
        if (required == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестное действие.");
        if (!required.equals(item.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Действие недоступно для статуса «" + item.getStatus() + "».");
        String next = switch (action.toUpperCase()) {
            case "ORDER_PARTS" -> { if (parts.findAllByRepairCase_IdOrderBySortOrderAscIdAsc(caseId).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сначала добавьте хотя бы одну деталь."); yield "WAITING_PARTS"; }
            case "SCHEDULE_REPAIR" -> "SCHEDULED";
            case "START_REPAIR" -> "IN_REPAIR";
            case "FINISH_REPAIR" -> { var order = workOrders.findByRepairCaseId(caseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя завершить ремонт без выполненных работ.")); if (order.getLines() == null || order.getLines().isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя завершить ремонт без выполненных работ."); yield "READY"; }
            case "DELIVER" -> "DELIVERED";
            case "CLOSE" -> "CLOSED";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестное действие.");
        };
        String previous = item.getStatus(); if (request != null) { if (request.contractorId() != null) item.setContractorId(request.contractorId()); if (request.appointmentDate() != null) item.setAppointmentDate(request.appointmentDate()); if (request.appointmentTime() != null) item.setAppointmentTime(request.appointmentTime()); if (request.receivedBy() != null) item.setReceivedBy(request.receivedBy().trim()); if (request.comment() != null) item.setComment(request.comment().trim()); } if ("START_REPAIR".equalsIgnoreCase(action)) item.setStartedAt(java.time.OffsetDateTime.now()); if ("DELIVER".equalsIgnoreCase(action)) item.setDeliveredAt(java.time.OffsetDateTime.now()); item.setStatus(next); item.setUpdatedBy(userId(authentication)); item.touch(); RepairCase saved = cases.save(item); record(saved, previous, next, historyAction(action, saved), userId(authentication)); return response(saved);
    }
    private String humanAction(String action) { return switch (action.toUpperCase()) { case "ORDER_PARTS" -> "Заказаны детали"; case "SCHEDULE_REPAIR" -> "Автомобиль записан на ремонт"; case "START_REPAIR" -> "Ремонт начат"; case "FINISH_REPAIR" -> "Ремонт завершён"; case "DELIVER" -> "Автомобиль выдан"; case "CLOSE" -> "Случай закрыт"; default -> "Изменение обращения"; }; }
    private String historyAction(String action, RepairCase item) { String result = humanAction(action); if ("SCHEDULE_REPAIR".equalsIgnoreCase(action)) { var contractor = item.getContractorId() == null ? null : contractors.findById(item.getContractorId()).orElse(null); result += ". Исполнитель: " + (contractor == null ? "не указан" : contractor.getShortName()) + ". Дата: " + (item.getAppointmentDate() == null ? "не указана" : item.getAppointmentDate()) + (item.getAppointmentTime() == null ? "" : ", время: " + item.getAppointmentTime()); } return result; }
    private Long userId(Authentication authentication) { Object principal = authentication == null ? null : authentication.getPrincipal(); return principal instanceof AppUserPrincipal user ? user.getUserId() : null; }
    private void record(RepairCase item,String previous,String next,String comment,Long userId){ RepairCaseHistory event=new RepairCaseHistory(); event.setRepairCase(item); event.setPreviousStatus(previous); event.setNewStatus(next); event.setComment(comment); event.setCreatedBy(userId); history.save(event); }
    private String contractorAction(RepairCase item) { var contractor = item.getContractorId() == null ? null : contractors.findById(item.getContractorId()).orElse(null); return "Исполнитель назначен: " + (contractor == null ? "не указан" : contractor.getShortName()); }
    private RepairCase findCase(Long carId, Long caseId) { return cases.findById(caseId).filter(value -> value.getCar().getId().equals(carId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страховой случай не найден.")); }
    private RepairCaseResponse response(RepairCase item) { String name = item.getCreatedBy() == null ? null : users.findById(item.getCreatedBy()).map(user -> user.getName()).orElse("Пользователь #" + item.getCreatedBy()); return RepairCaseResponse.from(item, name); }
    private Car ensureCar(Long id) { return cars.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден.")); }
    private void ensureEditable(RepairCase item) { if ("CLOSED".equals(item.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Закрытый страховой случай доступен только для просмотра."); }
    private String value(String value) { return value == null ? "" : value.trim(); }
}
