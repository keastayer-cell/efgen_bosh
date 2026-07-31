package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.Counterparty;
import com.efgenbosh.backend.dto.directory.CounterpartyRequest;
import com.efgenbosh.backend.dto.directory.CounterpartyResponse;
import com.efgenbosh.backend.repository.CounterpartyRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/counterparties")
public class CounterpartyController {
    private final CounterpartyRepository repository;
    public CounterpartyController(CounterpartyRepository repository) { this.repository = repository; }

    @GetMapping
    public List<CounterpartyResponse> findAll() {
        return repository.findAllByActiveTrueOrderByNameAsc().stream().map(CounterpartyResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CounterpartyResponse create(@Valid @RequestBody CounterpartyRequest request) {
        Counterparty item = new Counterparty();
        item.setName(request.name().trim());
        item.setInn(value(request.inn())); item.setAddress(value(request.address()));
        item.setPhone(value(request.phone())); item.setNote(value(request.note()));
        return CounterpartyResponse.from(repository.save(item));
    }

    @PutMapping("/{id}")
    public CounterpartyResponse update(@PathVariable Long id, @Valid @RequestBody CounterpartyRequest request) {
        Counterparty item = repository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Контрагент не найден."));
        item.setName(request.name().trim()); item.setInn(value(request.inn())); item.setAddress(value(request.address()));
        item.setPhone(value(request.phone())); item.setNote(value(request.note()));
        return CounterpartyResponse.from(repository.save(item));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(item -> { item.setActive(false); repository.save(item); });
    }

    private String value(String value) { return value == null ? "" : value.trim(); }
}
