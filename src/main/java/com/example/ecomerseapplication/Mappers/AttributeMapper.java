package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.responses.CategoryAttributesResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.DetailedAttributeGroupsWithCategoryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfGroupProjection;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeMapper {

    public static List<CategoryAttributesResponse> attributeOptionListToCatAttrResponseList(
            List<AttributeOptionDTO> attributes) {

        return attributes.stream()
                .collect(Collectors.groupingBy(
                        AttributeOptionDTO::attributeName
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    String attributeName = entry.getKey();
                    List<AttributeOptionDTO> group = entry.getValue();

                    // Extract unique options
                    List<String> options = group.stream()
                            .map(AttributeOptionDTO::option)
                            .collect(Collectors.toList());

                    // Measurement unit: all entries in the same group have the same unit
                    String measurementUnit = group.getFirst().measurementUnit();

                    return new CategoryAttributesResponse(
                            attributeName,
                            options,
                            measurementUnit
                    );
                })
                .toList();
    }

    public static AttributeOfGroupDTO attributeOfGroupProjToDto(AttributeOfGroupProjection projection) {
        return new AttributeOfGroupDTO(
                projection.getName(),
                projection.getMeasurementUnit()
        );
    }

    public static List<AttributeOfGroupDTO> attributeOfGroupProjListToDtoList(List<AttributeOfGroupProjection> projections) {
        return projections.stream().map(AttributeMapper::attributeOfGroupProjToDto).toList();
    }

    public static DetailedAttributeGroupsWithCategoryResponse attributeGroupWCatProjToResponse(AttributeGroupsWithCategoryProjection projection, List<AttributeOfGroupDTO> attributesDto) {
        return new DetailedAttributeGroupsWithCategoryResponse(
                projection.getName(),
                projection.getIsInCategory(),
                attributesDto
        );
    }

}
