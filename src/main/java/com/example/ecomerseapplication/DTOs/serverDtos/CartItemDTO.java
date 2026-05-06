package com.example.ecomerseapplication.DTOs.serverDtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record CartItemDTO(
        CompactProductDto compactProductDto,
        Instant dateAdded,
        short quantity,
        int stockQuantity
) {
}
