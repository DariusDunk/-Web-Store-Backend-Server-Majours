package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductForCompactPurchaseHistoryResponse(
        @JsonProperty("product_name")
        String productName,
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("image_url")
        String imageUrl
        ) {
}
