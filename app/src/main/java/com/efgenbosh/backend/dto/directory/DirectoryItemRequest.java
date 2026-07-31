package com.efgenbosh.backend.dto.directory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record DirectoryItemRequest(@NotBlank @Size(max = 255) String name, String legalDetails) { }
