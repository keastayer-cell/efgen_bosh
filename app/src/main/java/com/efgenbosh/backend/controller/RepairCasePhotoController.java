package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.domain.CarPhoto;
import com.efgenbosh.backend.domain.RepairCase;
import com.efgenbosh.backend.dto.car.CarPhotoRequest;
import com.efgenbosh.backend.dto.car.CarPhotoResponse;
import com.efgenbosh.backend.repository.CarPhotoRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cars/{carId}/repair-cases/{caseId}/photos")
public class RepairCasePhotoController {
    private final CarPhotoRepository photos; private final RepairCaseRepository cases;
    public RepairCasePhotoController(CarPhotoRepository photos, RepairCaseRepository cases) { this.photos = photos; this.cases = cases; }
    @GetMapping public List<CarPhotoResponse> list(@PathVariable Long carId, @PathVariable Long caseId) { RepairCase item = repairCase(carId, caseId); return photos.findAllByRepairCase_IdOrderByCreatedAtDesc(item.getId()).stream().map(CarPhotoResponse::from).toList(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CarPhotoResponse add(@PathVariable Long carId, @PathVariable Long caseId, @Valid @RequestBody CarPhotoRequest request) { if (!request.dataUrl().startsWith("data:image/")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Разрешены только изображения."); RepairCase item = repairCase(carId, caseId); if (photos.findAllByRepairCase_IdOrderByCreatedAtDesc(item.getId()).size() >= 20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Для случая можно сохранить не более 20 фото."); CarPhoto photo = new CarPhoto(); photo.setCar(item.getCar()); photo.setRepairCase(item); photo.setFileName(request.fileName()); photo.setMimeType(request.mimeType()); photo.setDataUrl(request.dataUrl()); return CarPhotoResponse.from(photos.save(photo)); }
    @DeleteMapping("/{photoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long carId, @PathVariable Long caseId, @PathVariable Long photoId) { RepairCase item = repairCase(carId, caseId); CarPhoto photo = photos.findById(photoId).filter(p -> p.getRepairCase() != null && p.getRepairCase().getId().equals(item.getId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото не найдено.")); photos.delete(photo); }
    private RepairCase repairCase(Long carId, Long caseId) { return cases.findById(caseId).filter(x -> x.getCar().getId().equals(carId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страховой случай не найден.")); }
}
