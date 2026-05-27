package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttributeOfProductResponse(
        @JsonProperty("id")
        Integer attributeNameId,
        @JsonProperty("attribute_name")
        String attributeName,
        @JsonProperty("attribute_value")
        String attributeValue,
        @JsonProperty("measurement_unit")
        String measurementUnit,
        @JsonProperty("is_in_category")
        Boolean isInCategory
        ) {
}
