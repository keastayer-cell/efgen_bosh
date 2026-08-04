package com.efgenbosh.backend.dto.directory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VehicleCatalogItemRequest(@NotBlank @Size(max = 120) String name, Long makeId) { }
