package com.example.ecomerseapplication.DTOs.responses;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetailedAttributeGroupResponse(
        @JsonProperty("group_name")
        String groupName,
        @JsonProperty("attributes")
        List<AttributeOfGroupDTO> attributes
) {
}
