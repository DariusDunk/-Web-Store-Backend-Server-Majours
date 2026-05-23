package com.example.ecomerseapplication.DTOs.serverDtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttributeOfGroupDTO(
        @JsonProperty("name")
        String name,
        @JsonProperty("measurement_unit")
        String measurementUnit
        ) {
}
