package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CatManRequest(
        @JsonProperty("categories")
        List<String> categories,
        @JsonProperty("manufacturers")
        List<String> manufacturers,
        @JsonProperty("page")
        int page
        ) {
}
