package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ManufacturerFormRequest(
        @JsonProperty("name")
        String name
        ) {
}
