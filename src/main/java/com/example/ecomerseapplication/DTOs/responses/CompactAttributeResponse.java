package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompactAttributeResponse(
        @JsonProperty("id")
        Integer attributeNameId,
        @JsonProperty("attribute_name")
        String attributeName,
        @JsonProperty("measurement_unit")
        String measurementUnit) {
}
