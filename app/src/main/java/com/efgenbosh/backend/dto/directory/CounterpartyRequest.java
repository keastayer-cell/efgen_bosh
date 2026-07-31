package com.efgenbosh.backend.dto.directory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CounterpartyRequest(
    @NotBlank @Size(max = 500) String name,
    @Size(max = 20) String inn,
    String address,
    @Size(max = 40) String phone,
    String note
) { }
