package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SaleProductSuggestionResponse(
        @JsonProperty("name")
        String name,
        @JsonProperty("product_code")
        String productCode
) {
}
