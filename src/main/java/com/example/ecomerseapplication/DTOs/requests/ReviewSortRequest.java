package com.example.ecomerseapplication.DTOs.requests;

import com.example.ecomerseapplication.enums.SortType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewSortRequest(
        @NotBlank
        @JsonProperty("product_code")
        String productCode,
        @NotNull
        @JsonProperty("page")
        int page,
        @NotNull
        @JsonProperty("sort_order")
        SortType sortOrder,
        @NotNull
        @JsonProperty("verified_only")
        Boolean verifiedOnly,
        @JsonProperty("rating_value")
        Short ratingValue
        ) {
}
