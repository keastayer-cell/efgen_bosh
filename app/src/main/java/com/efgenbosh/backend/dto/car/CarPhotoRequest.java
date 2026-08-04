package com.efgenbosh.backend.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarPhotoRequest(@Size(max = 255) String fileName, @Size(max = 100) String mimeType,
                              @NotBlank @Size(max = 12_000_000) String dataUrl) {}
