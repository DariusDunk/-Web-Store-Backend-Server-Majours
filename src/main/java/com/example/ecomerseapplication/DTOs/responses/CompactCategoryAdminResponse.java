package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompactCategoryAdminResponse(
        @JsonProperty("name")
        String name,
        Integer id,
        @JsonProperty("is_deleted")
        Boolean isDeleted
        ) {
}
