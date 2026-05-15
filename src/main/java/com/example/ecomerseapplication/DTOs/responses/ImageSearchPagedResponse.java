package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ImageSearchPagedResponse(
        @JsonProperty("product_page")
        PageResponse<CompactProductResponse> products,
        @JsonProperty("categories")
        List<String> categoryNames,
        @JsonProperty("manufacturers")
        List<String> manufacturerNames
        ) {
}
