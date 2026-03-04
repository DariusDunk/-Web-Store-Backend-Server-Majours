package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReviewCreateRequest(
        @NotBlank
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("review_text")
        String reviewText,
        @Positive
        short rating) {
}
