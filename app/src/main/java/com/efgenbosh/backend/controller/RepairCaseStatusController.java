package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.car.RepairCaseStatusResponse;
import java.util.List;
import com.efgenbosh.backend.repository.RepairCaseStatusRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repair-case-statuses")
public class RepairCaseStatusController {
    private final RepairCaseStatusRepository statuses;
    public RepairCaseStatusController(RepairCaseStatusRepository statuses) { this.statuses = statuses; }
    @GetMapping
    public List<RepairCaseStatusResponse> list() { return statuses.findAllByActiveTrueOrderBySortOrderAsc().stream().map(item -> new RepairCaseStatusResponse(item.getId(), item.getCode(), item.getLabel())).toList(); }
}
