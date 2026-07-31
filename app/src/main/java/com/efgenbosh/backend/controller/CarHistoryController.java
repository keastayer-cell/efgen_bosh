package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.car.CarHistoryResponse;
import com.efgenbosh.backend.repository.CarHistoryRepository;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cars/{carId}/history")
public class CarHistoryController {
    private final CarHistoryRepository repository;
    public CarHistoryController(CarHistoryRepository repository) { this.repository = repository; }
    @GetMapping public List<CarHistoryResponse> find(@PathVariable Long carId) { return repository.findAllByCar_IdOrderByCreatedAtDesc(carId).stream().map(CarHistoryResponse::from).toList(); }
}
