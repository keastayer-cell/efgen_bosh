package com.efgenbosh.backend.dto.directory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContractorRequest(@Size(max = 80) String code, @NotBlank @Size(max = 255) String shortName,
    @NotBlank String fullName, @NotBlank @Size(max = 40) String phone, String signerName, String inn, String ogrnip, String address, String bankName,
    String bankInn, String bankKpp, String bik, String correspondentAccount, String settlementAccount) { }
