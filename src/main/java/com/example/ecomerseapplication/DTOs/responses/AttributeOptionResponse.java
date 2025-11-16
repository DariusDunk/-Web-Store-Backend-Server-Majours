package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttributeOptionResponse(

        String attributeName,
        String option,
        String measurementUnit
) {
}
