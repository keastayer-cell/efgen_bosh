package com.efgenbosh.backend.dto.directory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VehicleAliasRequest(@NotBlank @Size(max = 255) String sourceName,
                                  @Size(max = 255) String normalizedLatinName) { }
