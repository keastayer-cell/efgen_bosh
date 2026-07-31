package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.GeneratedDocument;
import com.efgenbosh.backend.dto.workorder.GeneratedDocumentResponse;
import com.efgenbosh.backend.repository.GeneratedDocumentRepository;
import com.efgenbosh.backend.repository.WorkOrderRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/work-orders/{workOrderId}/documents")
public class GeneratedDocumentController {
    private final GeneratedDocumentRepository documents; private final WorkOrderRepository orders;
    public GeneratedDocumentController(GeneratedDocumentRepository documents, WorkOrderRepository orders) { this.documents = documents; this.orders = orders; }
    @GetMapping public List<GeneratedDocumentResponse> list(@PathVariable Long workOrderId) { return documents.findAllByWorkOrder_IdOrderByCreatedAtDesc(workOrderId).stream().map(GeneratedDocumentResponse::from).toList(); }
    @PostMapping("/{type}") @ResponseStatus(HttpStatus.CREATED)
    public GeneratedDocumentResponse create(@PathVariable Long workOrderId, @PathVariable String type, @RequestParam(defaultValue = "") String number) {
        if (!List.of("order", "invoice", "act", "acceptance", "delivery", "bundle").contains(type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестный тип документа.");
        GeneratedDocument d = new GeneratedDocument(); d.setWorkOrder(orders.findById(workOrderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ-наряд не найден."))); d.setDocumentType(type); d.setDocumentNumber(number == null ? "" : number); return GeneratedDocumentResponse.from(documents.save(d));
    }
}
