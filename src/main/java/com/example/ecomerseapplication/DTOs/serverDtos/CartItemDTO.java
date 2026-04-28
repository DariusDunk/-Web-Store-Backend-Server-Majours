package com.example.ecomerseapplication.DTOs.serverDtos;

import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;

import java.time.Instant;

public record CartItemDTO(
        CompactProductDto compactProductDto,
        Instant dateAdded,
        short quantity
) {
}
