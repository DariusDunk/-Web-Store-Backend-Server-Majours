package com.example.ecomerseapplication.DTOs.serverDtos;

import java.util.Map;

public record PredictionResponseDTO(
        Map<String, Float> categoryPredictions,
         Map<String, Float> manufacturerPredictions) {
}
