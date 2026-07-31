package com.efgenbosh.backend.dto.directory;

import com.efgenbosh.backend.domain.VehicleAlias;

public record VehicleAliasResponse(Long id, String sourceName, String normalizedLatinName) {
    public static VehicleAliasResponse from(VehicleAlias item) {
        return new VehicleAliasResponse(item.getId(), item.getSourceName(), item.getNormalizedLatinName());
    }
}
