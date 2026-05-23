package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCategoryRequest(
        @NotNull
        Integer id,
        @NotBlank
        String name,
        @NotNull
        @JsonProperty("is_deleted")
        Boolean isDeleted,
        @JsonProperty("attribute_groups")
        List<String> attributeGroups

) {
}
