package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.DefectAnalysis;
import com.efgenbosh.backend.dto.defect.DefectAnalysisRequest;
import com.efgenbosh.backend.dto.defect.DefectAnalysisResponse;
import com.efgenbosh.backend.repository.CarRepository;
import com.efgenbosh.backend.repository.DefectAnalysisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cars/{carId}/defect-analysis")
public class DefectAnalysisController {
    private final DefectAnalysisRepository repository;
    private final CarRepository carRepository;
    private final ObjectMapper mapper;
    public DefectAnalysisController(DefectAnalysisRepository repository, CarRepository carRepository, ObjectMapper mapper) {
        this.repository = repository; this.carRepository = carRepository; this.mapper = mapper;
    }
    @GetMapping
    public DefectAnalysisResponse find(@PathVariable Long carId) {
        DefectAnalysis item = repository.findByCar_Id(carId).orElseGet(() -> create(carId));
        return DefectAnalysisResponse.from(item, mapper);
    }
    @PutMapping
    public DefectAnalysisResponse save(@PathVariable Long carId, @Valid @RequestBody DefectAnalysisRequest request) {
        DefectAnalysis item = repository.findByCar_Id(carId).orElseGet(() -> create(carId));
        try {
            item.setStatus(request.status()); item.setFindings(request.findings().trim());
            item.setRecommendations(request.recommendations().trim());
            item.setPhotosJson(mapper.writeValueAsString(request.photos().stream().limit(8).toList())); item.touch();
            return DefectAnalysisResponse.from(repository.save(item), mapper);
        } catch (Exception error) { throw new IllegalStateException("Не удалось сохранить дефектовку.", error); }
    }
    private DefectAnalysis create(Long carId) {
        DefectAnalysis item = new DefectAnalysis();
        item.setCar(carRepository.findById(carId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден.")));
        return repository.save(item);
    }
}
