package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.responses.CategoryAttributesResponse;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeMapper {

    public static Set<CategoryAttributesResponse> attributeOptionListToCatAttrResponseSet(List<AttributeOptionDTO> attributes) {
        Set<CategoryAttributesResponse> categoryAttributesResponses = new HashSet<>();

        Map<String, Set<String>> categoryAttributesResponsesMap = attributes.
                stream().
                collect(Collectors.groupingBy(
                        AttributeOptionDTO::attributeName,
                        LinkedHashMap::new,
                        Collectors.mapping(AttributeOptionDTO::option,Collectors.toSet())

                ));

        for (Map.Entry<String, Set<String>> entry : categoryAttributesResponsesMap.entrySet()) {

            String measurement = "";

            for (AttributeOptionDTO attributeOptionDTO : attributes) {
                if (attributeOptionDTO.attributeName().equals(entry.getKey())) {
                    measurement = attributeOptionDTO.measurementUnit();
                    break;
                }
            }

            categoryAttributesResponses.add(new CategoryAttributesResponse(entry.getKey(),entry.getValue(),measurement));
        }

        return categoryAttributesResponses;
    }

//    public static AttributeOptionResponse attributeOptionToResponse(CategoryAttribute entity) {
//        AttributeOptionResponse response = new AttributeOptionDTO(
//          entity.getAttributeName().getAttributeName(),
//          entity.getAttributeOption(),
//          entity.get
//        );
//    }
}
