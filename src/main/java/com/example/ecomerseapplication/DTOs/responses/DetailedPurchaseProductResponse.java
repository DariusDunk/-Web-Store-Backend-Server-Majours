package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetailedPurchaseProductResponse(
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("product_name")
        String name,
        @JsonProperty("single_price")
        int singlePrice,
        short rating,
        @JsonProperty("review_count")
        int reviewCount,
        @JsonProperty("image_url")
        String imageUrl,
        int quantity) {
}
