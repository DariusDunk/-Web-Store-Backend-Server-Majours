package com.example.ecomerseapplication.DTOs.requests;

import com.example.ecomerseapplication.enums.SortType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ReviewSortRequest(
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("user_id")
        long userId,
        @JsonProperty("page")
        int page,
        @JsonProperty("sort_order")
        SortType sortOrder,
        @JsonProperty("verified_only")
        Boolean verifiedOnly,
        @JsonProperty("rating_value")
        Short ratingValue
        ) {
}
