package com.efgenbosh.backend.dto.directory;

import java.math.BigDecimal;
public record WorkCatalogItemResponse(Long id, String code, String name, String categoryName, String defaultUnit, BigDecimal normHours) { }
