package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductFormRequest(
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
        @JsonProperty("description")
        String description,
        @JsonProperty("model")
        String model
        ) {
}
