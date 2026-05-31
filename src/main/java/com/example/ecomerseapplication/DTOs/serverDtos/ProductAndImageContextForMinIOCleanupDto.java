package com.example.ecomerseapplication.DTOs.serverDtos;

import java.util.Set;

public record ProductAndImageContextForMinIOCleanupDto(
        String productCode,
        Set<String> imageNames
) {
}
