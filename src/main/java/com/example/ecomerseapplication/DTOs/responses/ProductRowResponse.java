package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductRowResponse(
        @JsonProperty("type")
        String type,
        @JsonProperty("name")
        String name,
        @JsonProperty("products")
        List<CompactProductResponse> products) {

}
