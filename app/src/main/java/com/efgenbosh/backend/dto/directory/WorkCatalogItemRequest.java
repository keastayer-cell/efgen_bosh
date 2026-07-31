package com.efgenbosh.backend.dto.directory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkCatalogItemRequest(
    @NotBlank @Size(max = 120) String code,
    @NotBlank @Size(max = 500) String name,
    @NotBlank @Size(max = 255) String categoryName,
    @NotBlank @Size(max = 40) String defaultUnit
) { }
