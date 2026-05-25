package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductFormRequest(
        @JsonProperty("name")
        String productName,
        @JsonProperty("category_name")
        String categoryName,
        @JsonProperty("price")
        Integer originalPriceStotinki,
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("stock_quantity")
        Integer stockQuantity,
        @JsonProperty("manufacturer_name")
        String manufacturerName,
        @JsonProperty("description")
        String description
) {
}
