package com.efgenbosh.backend.controller;
import com.efgenbosh.backend.dto.car.RepairCaseRegistryResponse;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/repair-cases")
public class RepairCaseRegistryController {
    private final RepairCaseRepository cases;
    public RepairCaseRegistryController(RepairCaseRepository cases) { this.cases = cases; }
    @GetMapping public List<RepairCaseRegistryResponse> list() { return cases.findAllByOrderByCreatedAtDesc().stream().map(RepairCaseRegistryResponse::from).toList(); }
}
