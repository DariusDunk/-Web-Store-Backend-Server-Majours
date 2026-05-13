package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public record ImageSearchPagedResponse(
        @JsonProperty("products")
        PageResponse<CompactProductResponse> products,
        @JsonProperty("category_names")
        List<String> categoryNames,
        @JsonProperty("manufacturer_names")
        List<String> manufacturerNames
        ) {
}
