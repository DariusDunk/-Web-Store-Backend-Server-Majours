package com.example.ecomerseapplication.DTOs.serverDtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetailedAttributeGroupsWithCategoryResponse(
        @JsonProperty("group_name")
        String groupName,
        @JsonProperty("is_in_category")
        Boolean isInCategory,
        @JsonProperty("attributes")
        List<AttributeOfGroupDTO> attributes
        ) {
}
