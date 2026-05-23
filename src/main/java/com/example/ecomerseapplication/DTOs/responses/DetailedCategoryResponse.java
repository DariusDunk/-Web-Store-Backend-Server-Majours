package com.example.ecomerseapplication.DTOs.responses;

import com.example.ecomerseapplication.DTOs.serverDtos.DetailedAttributeGroupsWithCategoryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetailedCategoryResponse(@JsonProperty("name")
                                       String name,
                                       @JsonProperty("attribute_groups")
                                       List<DetailedAttributeGroupsWithCategoryResponse> attributeGroups) {
}
