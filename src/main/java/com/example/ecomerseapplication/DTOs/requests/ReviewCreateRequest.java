package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull
        @JsonProperty("product_code")
        String productCode,
        @NotNull
        @JsonProperty("review_text")
        String reviewText,
        @NotNull
        short rating) {
}
