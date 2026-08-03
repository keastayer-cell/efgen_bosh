package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.Car;
import com.efgenbosh.backend.domain.WorkOrder;
import com.efgenbosh.backend.domain.WorkOrderLine;
import com.efgenbosh.backend.domain.WorkOrderPartLine;
import com.efgenbosh.backend.domain.RepairCase;
import com.efgenbosh.backend.dto.workorder.WorkOrderLineRequest;
import com.efgenbosh.backend.dto.workorder.WorkOrderPartLineRequest;
import com.efgenbosh.backend.dto.workorder.WorkOrderRequest;
import com.efgenbosh.backend.dto.workorder.WorkOrderResponse;
import com.efgenbosh.backend.repository.CarRepository;
import com.efgenbosh.backend.repository.WorkOrderRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import com.efgenbosh.backend.repository.RepairCaseHistoryRepository;
import com.efgenbosh.backend.domain.RepairCaseHistory;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class WorkOrderService {
    private final WorkOrderRepository orders;
    private final CarRepository cars;
    private final RepairCaseRepository repairCases;
    private final RepairCaseHistoryRepository history;

    public WorkOrderService(WorkOrderRepository orders, CarRepository cars, RepairCaseRepository repairCases, RepairCaseHistoryRepository history) {
        this.orders = orders; this.cars = cars; this.repairCases = repairCases; this.history = history;
    }

    @Transactional
    public WorkOrderResponse findByCarId(Long carId) {
        return WorkOrderResponse.from(orders.findByCarId(carId).orElseGet(() -> createDraft(carId, null)));
    }

    @Transactional
    public WorkOrderResponse save(Long carId, WorkOrderRequest request, Long userId) {
        Car car = cars.findByIdWithParts(carId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден."));
        WorkOrder order = orders.findByCarId(carId).orElseGet(() -> createDraft(carId, userId));
        if (request.documentDate() != null) order.setDocumentDate(request.documentDate());
        order.setCustomer(value(request.customer()));
        order.setOrderNumber(value(request.orderNumber()));
        order.setInvoiceNumber(value(request.invoiceNumber()));
        order.setActNumber(value(request.actNumber()));
        if (request.status() != null && List.of("DRAFT", "READY", "CLOSED").contains(request.status().toUpperCase())) {
            order.setStatus(request.status().toUpperCase());
        }
        order.replaceLines((request.lines() == null ? List.<WorkOrderLineRequest>of() : request.lines())
            .stream().map(this::line).toList());
        order.replacePartLines((request.partLines() == null ? List.<WorkOrderPartLineRequest>of() : request.partLines())
            .stream().map(this::partLine).toList());
        order.touch();
        return WorkOrderResponse.from(orders.save(order));
    }

    @Transactional
    public WorkOrderResponse findByCaseId(Long carId, Long caseId) {
        RepairCase repairCase = ensureCase(carId, caseId);
        return WorkOrderResponse.from(orders.findByRepairCaseId(caseId).orElseGet(() -> createCaseDraft(repairCase, null)));
    }

    @Transactional
    public WorkOrderResponse saveCase(Long carId, Long caseId, WorkOrderRequest request, Long userId) {
        RepairCase repairCase = ensureCase(carId, caseId);
        if ("DELIVERED".equals(repairCase.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Выданная машина доступна только для просмотра.");
        WorkOrder order = orders.findByRepairCaseId(caseId).orElseGet(() -> createCaseDraft(repairCase, userId));
        applyRequest(order, request); order.touch();
        WorkOrder saved = orders.save(order);
        recordWorkOrderChange(repairCase, saved, userId);
        return WorkOrderResponse.from(saved);
    }

    @Transactional
    public WorkOrderResponse createStandalone(WorkOrderRequest request, Long userId) {
        WorkOrder order = new WorkOrder();
        order.setCreatedBy(userId); order.setClaimNumber(""); order.setVehicleName("");
        order.setRegistrationNumber(""); order.setVin("");
        applyRequest(order, request);
        return WorkOrderResponse.from(orders.save(order));
    }

    @Transactional
    public WorkOrderResponse findById(Long id) {
        return WorkOrderResponse.from(orders.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ-наряд не найден.")));
    }

    private void applyRequest(WorkOrder order, WorkOrderRequest request) {
        if (request.documentDate() != null) order.setDocumentDate(request.documentDate());
        order.setCustomer(value(request.customer())); order.setOrderNumber(value(request.orderNumber()));
        order.setInvoiceNumber(value(request.invoiceNumber())); order.setActNumber(value(request.actNumber()));
        if (request.status() != null && List.of("DRAFT", "READY", "CLOSED").contains(request.status().toUpperCase())) order.setStatus(request.status().toUpperCase());
        order.replaceLines((request.lines() == null ? List.<WorkOrderLineRequest>of() : request.lines()).stream().map(this::line).toList());
        order.replacePartLines((request.partLines() == null ? List.<WorkOrderPartLineRequest>of() : request.partLines()).stream().map(this::partLine).toList());
        order.touch();
    }

    private WorkOrder createDraft(Long carId, Long userId) {
        Car car = cars.findByIdWithParts(carId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден."));
        WorkOrder order = new WorkOrder();
        order.setCar(car);
        order.setCreatedBy(userId);
        order.setClaimNumber(value(car.getClaimNumber()));
        order.setVehicleName(value(car.getVehicleName()));
        order.setRegistrationNumber(value(car.getRegistrationNumber()));
        order.setVin(value(car.getVin()));
        return orders.save(order);
    }

    private WorkOrder createCaseDraft(RepairCase repairCase, Long userId) {
        // У автомобиля может уже быть legacy-заказ-наряд с уникальным car_id.
        // Заказ-наряд обращения идентифицируется repair_case_id и не должен конфликтовать с ним.
        WorkOrder order = new WorkOrder(); order.setRepairCase(repairCase); order.setCreatedBy(userId);
        order.setClaimNumber(value(repairCase.getClaimNumber())); order.setVehicleName(value(repairCase.getCar().getVehicleName()));
        order.setRegistrationNumber(value(repairCase.getCar().getRegistrationNumber())); order.setVin(value(repairCase.getCar().getVin()));
        return orders.save(order);
    }

    private void recordWorkOrderChange(RepairCase repairCase, WorkOrder order, Long userId) {
        var event = new RepairCaseHistory();
        event.setRepairCase(repairCase);
        event.setPreviousStatus(repairCase.getStatus());
        event.setNewStatus(repairCase.getStatus());
        event.setComment("Работы сохранены: " + order.getLines().size() + ", материалов в заказ-наряде: " + order.getPartLines().size());
        event.setCreatedBy(userId);
        history.save(event);
    }

    private RepairCase ensureCase(Long carId, Long caseId) {
        return repairCases.findById(caseId).filter(item -> item.getCar().getId().equals(carId)).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Обращение не найдено."));
    }

    private WorkOrderLine line(WorkOrderLineRequest request) {
        WorkOrderLine line = new WorkOrderLine();
        line.setCategoryNameSnapshot(value(request.categoryName()));
        line.setNameSnapshot(request.name().trim());
        line.setUnit(request.unit().trim());
        line.setQuantity(request.quantity());
        line.setPrice(request.price().setScale(2));
        line.setContractorId(request.contractorId());
        line.setComment(value(request.comment()));
        line.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return line;
    }

    private WorkOrderPartLine partLine(WorkOrderPartLineRequest request) {
        WorkOrderPartLine line = new WorkOrderPartLine();
        line.setPartId(request.partId());
        line.setNameSnapshot(request.name().trim());
        line.setArticleSnapshot(value(request.article()));
        line.setQuantity(request.quantity());
        line.setPrice(request.price().setScale(2));
        line.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return line;
    }

    private String value(String value) { return value == null ? "" : value.trim(); }
}
