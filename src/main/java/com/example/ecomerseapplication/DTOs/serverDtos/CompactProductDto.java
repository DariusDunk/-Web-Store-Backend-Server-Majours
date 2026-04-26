package com.example.ecomerseapplication.DTOs.serverDtos;

public record CompactProductDto(
        String productCode,
        String name,
        int originalPriceStotinki,
        Short defaultSaleDiscount,
        Short explicitDiscount,
        short rating,
        int reviewCount,
        String imageUrl,
        boolean isInStock
) {
}
