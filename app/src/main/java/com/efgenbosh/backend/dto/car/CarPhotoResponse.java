package com.efgenbosh.backend.dto.car;

import com.efgenbosh.backend.domain.CarPhoto;
import java.time.OffsetDateTime;

public record CarPhotoResponse(Long id, Long carId, String fileName, String mimeType, String dataUrl, OffsetDateTime createdAt) {
    public static CarPhotoResponse from(CarPhoto photo) {
        return new CarPhotoResponse(photo.getId(), photo.getCar().getId(), photo.getFileName(), photo.getMimeType(), photo.getDataUrl(), photo.getCreatedAt());
    }
}
