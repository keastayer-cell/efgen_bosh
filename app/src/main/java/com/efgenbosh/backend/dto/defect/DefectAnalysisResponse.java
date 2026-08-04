package com.efgenbosh.backend.dto.defect;

import com.efgenbosh.backend.domain.DefectAnalysis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public record DefectAnalysisResponse(Long id, Long carId, String status, String findings,
                                     String recommendations, List<String> photos) {
    public static DefectAnalysisResponse from(DefectAnalysis item, ObjectMapper mapper) {
        try {
            return new DefectAnalysisResponse(item.getId(), item.getCar().getId(), item.getStatus(),
                item.getFindings(), item.getRecommendations(), mapper.readValue(item.getPhotosJson(), new TypeReference<>() { }));
        } catch (Exception error) { throw new IllegalStateException("Не удалось прочитать фотографии дефектовки.", error); }
    }
}
