package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AttributesOfProductAndCategory(
        @JsonProperty("category_attributes")
        List<CompactAttributeResponse> categoryAttributes,
        @JsonProperty("product_attributes")
        List<AttributeOfProductResponse> productAttributes
) {
}
