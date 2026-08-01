package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.RepairCaseStatus;
import com.efgenbosh.backend.dto.car.RepairCaseStatusResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repair-case-statuses")
public class RepairCaseStatusController {
    @GetMapping
    public List<RepairCaseStatusResponse> list() { return Arrays.stream(RepairCaseStatus.values()).map(RepairCaseStatusResponse::from).toList(); }
}
