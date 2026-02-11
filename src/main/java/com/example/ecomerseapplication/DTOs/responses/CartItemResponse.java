package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;

public record CartItemResponse(
        @JsonProperty("product")
        CompactProductResponse compactProductResponse,
        @JsonProperty("date_added")
        Instant dateAdded,
        short quantity
) {
}
