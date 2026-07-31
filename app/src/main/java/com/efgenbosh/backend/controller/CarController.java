package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.car.CarRequest;
import com.efgenbosh.backend.dto.car.CarResponse;
import com.efgenbosh.backend.dto.car.PartRequest;
import com.efgenbosh.backend.dto.car.PartResponse;
import com.efgenbosh.backend.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
public class CarController {
    private final CarService service;

    public CarController(CarService service) { this.service = service; }

    @GetMapping
    public List<CarResponse> findAll() { return service.findAll(); }

    @GetMapping("/search")
    public List<CarResponse> search(@RequestParam(required = false) String q) { return service.search(q); }

    @GetMapping("/{id}")
    public CarResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse create(@Valid @RequestBody CarRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CarResponse update(@PathVariable Long id, @Valid @RequestBody CarRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PatchMapping("/{id}/delivery")
    public CarResponse setDelivered(@PathVariable Long id, @RequestParam boolean delivered) {
        return service.setDelivered(id, delivered);
    }

    @PatchMapping("/{id}/acceptance")
    public CarResponse setAccepted(@PathVariable Long id, @RequestParam boolean accepted) {
        return service.setAccepted(id, accepted);
    }

    @PostMapping("/{carId}/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse addPart(@PathVariable Long carId, @Valid @RequestBody PartRequest request) {
        return service.addPart(carId, request);
    }

    @PutMapping("/{carId}/parts/{partId}")
    public PartResponse updatePart(@PathVariable Long carId, @PathVariable Long partId,
                                   @Valid @RequestBody PartRequest request) {
        return service.updatePart(carId, partId, request);
    }

    @PatchMapping("/{carId}/parts/{partId}/receipt")
    public PartResponse setPartReceived(@PathVariable Long carId, @PathVariable Long partId,
                                        @RequestParam boolean received) {
        return service.setPartReceived(carId, partId, received);
    }

    @DeleteMapping("/{carId}/parts/{partId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePart(@PathVariable Long carId, @PathVariable Long partId) {
        service.deletePart(carId, partId);
    }
}
