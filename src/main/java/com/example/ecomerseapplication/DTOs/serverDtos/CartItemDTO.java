package com.example.ecomerseapplication.DTOs.serverDtos;

import java.time.Instant;

public record CartItemDTO(
        CompactProductDto compactProductDto,
        Instant dateAdded,
        short quantity
) {
}
