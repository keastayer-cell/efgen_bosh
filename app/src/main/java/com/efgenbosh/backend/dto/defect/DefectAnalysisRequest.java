package com.efgenbosh.backend.dto.defect;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DefectAnalysisRequest(@NotNull String status, @NotNull String findings,
                                    @NotNull String recommendations, @NotNull List<String> photos) { }
