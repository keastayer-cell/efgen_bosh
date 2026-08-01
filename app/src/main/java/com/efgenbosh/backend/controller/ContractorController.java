package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.Contractor;
import com.efgenbosh.backend.dto.directory.ContractorRequest;
import com.efgenbosh.backend.dto.directory.ContractorResponse;
import com.efgenbosh.backend.repository.ContractorRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/contractors")
public class ContractorController {
    private final ContractorRepository repository;
    public ContractorController(ContractorRepository repository) { this.repository = repository; }
    @GetMapping public List<ContractorResponse> findAll() { return repository.findAllByActiveTrueOrderBySortOrderAscShortNameAsc().stream().map(ContractorResponse::from).toList(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ContractorResponse create(@Valid @RequestBody ContractorRequest r) { return ContractorResponse.from(repository.save(apply(new Contractor(), r))); }
    @PutMapping("/{id}") public ContractorResponse update(@PathVariable Long id, @Valid @RequestBody ContractorRequest r) { return ContractorResponse.from(repository.save(apply(repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Исполнитель не найден.")), r))); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { repository.findById(id).ifPresent(c -> { c.setActive(false); repository.save(c); }); }
    private Contractor apply(Contractor c, ContractorRequest r) { c.setCode(r.code() == null || r.code().isBlank() ? (c.getCode() == null || c.getCode().isBlank() ? "MASTER-" + java.util.UUID.randomUUID() : c.getCode()) : r.code().trim()); c.setShortName(r.shortName().trim()); c.setFullName(r.fullName().trim()); c.setPhone(r.phone().trim()); c.setSignerName(v(r.signerName())); c.setInn(v(r.inn())); c.setOgrnip(v(r.ogrnip())); c.setAddress(v(r.address())); c.setBankName(v(r.bankName())); c.setBankInn(v(r.bankInn())); c.setBankKpp(v(r.bankKpp())); c.setBik(v(r.bik())); c.setCorrespondentAccount(v(r.correspondentAccount())); c.setSettlementAccount(v(r.settlementAccount())); return c; }
    private String v(String value) { return value == null ? "" : value.trim(); }
}
