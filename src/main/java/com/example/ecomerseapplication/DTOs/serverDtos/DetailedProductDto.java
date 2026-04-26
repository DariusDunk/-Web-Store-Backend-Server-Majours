package com.example.ecomerseapplication.DTOs.serverDtos;

import com.example.ecomerseapplication.DTOs.responses.AttributeOptionResponse;

import java.util.List;
import java.util.Set;

public record DetailedProductDto(
        String name,
        String categoryName,
        int originalPriceStotinki,
        Short defaultSaleDiscount,
        Short explicitDiscount,
        String productCode,
        String manufacturer,
        Set<AttributeOptionResponse> attributes,
        String productDescription,
        short rating,
        short deliveryCost,
        String model,
        List<String> productImageURLs,
        boolean inFavourites,
        boolean inCart,
        boolean reviewed,
        boolean isInStock
) {
}
