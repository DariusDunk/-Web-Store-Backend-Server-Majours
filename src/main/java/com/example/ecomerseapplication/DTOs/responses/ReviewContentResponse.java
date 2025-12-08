package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReviewContentResponse(
        @JsonProperty("review_text")
        String reviewText,
        Short rating,
        Boolean exists
) {
}
