package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.CarPhoto;
import com.efgenbosh.backend.dto.car.CarPhotoRequest;
import com.efgenbosh.backend.dto.car.CarPhotoResponse;
import com.efgenbosh.backend.repository.CarPhotoRepository;
import com.efgenbosh.backend.repository.CarRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cars/{carId}/photos")
public class CarPhotoController {
    private final CarPhotoRepository photos;
    private final CarRepository cars;
    public CarPhotoController(CarPhotoRepository photos, CarRepository cars) { this.photos = photos; this.cars = cars; }

    @GetMapping
    public List<CarPhotoResponse> list(@PathVariable Long carId) {
        ensureCar(carId);
        return photos.findAllByCar_IdOrderByCreatedAtDesc(carId).stream().map(CarPhotoResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarPhotoResponse add(@PathVariable Long carId, @Valid @RequestBody CarPhotoRequest request) {
        if (!request.dataUrl().startsWith("data:image/")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Разрешены только изображения.");
        if (photos.findAllByCar_IdOrderByCreatedAtDesc(carId).size() >= 20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Для автомобиля можно сохранить не более 20 фото.");
        CarPhoto photo = new CarPhoto(); photo.setCar(ensureCar(carId)); photo.setFileName(request.fileName()); photo.setMimeType(request.mimeType()); photo.setDataUrl(request.dataUrl());
        return CarPhotoResponse.from(photos.save(photo));
    }

    @DeleteMapping("/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long carId, @PathVariable Long photoId) {
        CarPhoto photo = photos.findById(photoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото не найдено."));
        if (!photo.getCar().getId().equals(carId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото не найдено.");
        photos.delete(photo);
    }

    private com.efgenbosh.backend.domain.Car ensureCar(Long id) { return cars.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден.")); }
}
