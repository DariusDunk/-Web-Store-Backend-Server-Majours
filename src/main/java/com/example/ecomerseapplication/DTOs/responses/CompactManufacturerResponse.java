package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompactManufacturerResponse
        (
                @JsonProperty("name")
                String name,
                Integer id
                ) {
}
