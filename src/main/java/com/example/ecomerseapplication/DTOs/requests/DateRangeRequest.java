package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record DateRangeRequest(
        @NotNull
        @JsonProperty("start_date")
        Instant startDate,
        @NotNull
        @JsonProperty("end_date")
        Instant endDate,
        @NotBlank
        @JsonProperty("timezone")
        String timezone) {
}
