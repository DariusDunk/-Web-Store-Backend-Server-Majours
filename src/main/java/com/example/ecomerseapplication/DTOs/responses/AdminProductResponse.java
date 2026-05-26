package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductResponse(
        @JsonProperty("id")
        Integer productId,
        @JsonProperty("name")
        String productName,
        @JsonProperty("category_id")
        Integer categoryId,
        @JsonProperty("price")
        Integer originalPriceStotinki,
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("stock_quantity")
        Integer stockQuantity,
        @JsonProperty("manufacturer_id")
        Integer manufacturerId,
        @JsonProperty("model")
        String model,
        @JsonProperty("description")
        String description,
        @JsonProperty("category_name")
        String categoryName,
        @JsonProperty("manufacturer_name")
        String manufacturerName
        ) {
}
