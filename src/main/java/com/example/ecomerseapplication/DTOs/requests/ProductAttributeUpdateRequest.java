package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductAttributeUpdateRequest(
        @JsonProperty("name_id")
        Integer nameId,
        @JsonProperty("value")
        String value
        ) {
}
