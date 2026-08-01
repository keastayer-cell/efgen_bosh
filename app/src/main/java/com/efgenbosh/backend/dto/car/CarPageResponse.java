package com.efgenbosh.backend.dto.car;

import java.util.List;

public record CarPageResponse(List<CarResponse> items, int page, int size, long totalPages, long totalItems, CarSearchSummary summary) { }
