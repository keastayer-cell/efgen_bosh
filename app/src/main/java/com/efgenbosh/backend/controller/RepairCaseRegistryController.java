package com.efgenbosh.backend.controller;
import com.efgenbosh.backend.dto.car.RepairCaseRegistryResponse;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import com.efgenbosh.backend.repository.PartRepository;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
@RestController @RequestMapping("/api/v1/repair-cases")
public class RepairCaseRegistryController {
    private final RepairCaseRepository cases; private final PartRepository parts;
    public RepairCaseRegistryController(RepairCaseRepository cases, PartRepository parts) { this.cases = cases; this.parts = parts; }
    @GetMapping @Transactional(readOnly = true) public List<RepairCaseRegistryResponse> list() { return cases.findAllByOrderByCreatedAtDesc().stream().map(item -> { var items = parts.findAllByRepairCase_IdOrderBySortOrderAscIdAsc(item.getId()); return RepairCaseRegistryResponse.from(item, items.size(), (int) items.stream().filter(com.efgenbosh.backend.domain.Part::isReceived).count(), (int) items.stream().filter(com.efgenbosh.backend.domain.Part::isDeclined).count()); }).toList(); }
}
