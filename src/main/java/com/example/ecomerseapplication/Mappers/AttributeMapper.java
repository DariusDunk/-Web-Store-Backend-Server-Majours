package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.responses.CategoryAttributesResponse;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeMapper {

//    public static Set<CategoryAttributesResponse> attributeOptionListToCatAttrResponseSet(List<AttributeOptionDTO> attributes) {
//        Set<CategoryAttributesResponse> categoryAttributesResponses = new HashSet<>();
//
//        Map<String, Set<String>> categoryAttributesResponsesMap = attributes.
//                stream().
//                collect(Collectors.groupingBy(
//                        AttributeOptionDTO::attributeName,
//                        LinkedHashMap::new,
//                        Collectors.mapping(AttributeOptionDTO::option,Collectors.toSet())
//
//                ));
//
//        for (Map.Entry<String, Set<String>> entry : categoryAttributesResponsesMap.entrySet()) {
//
//            String measurement = "";
//
//            for (AttributeOptionDTO attributeOptionDTO : attributes) {
//                if (attributeOptionDTO.attributeName().equals(entry.getKey())) {
//                    measurement = attributeOptionDTO.measurementUnit();
//                    break;
//                }
//            }
//
//            categoryAttributesResponses.add(new CategoryAttributesResponse(entry.getKey(),entry.getValue(),measurement));
//        }
//
//        return categoryAttributesResponses;
//    }
//
//    public static List<CategoryAttributesResponse> attributeOptionListToCatAttrResponseList(List<AttributeOptionDTO> attributes) {
////        Set<CategoryAttributesResponse> categoryAttributesResponses = new HashSet<>();
//        List<CategoryAttributesResponse> categoryAttributesResponses = new ArrayList<>();
////
////        Map<String, List<String>> categoryAttributesResponsesMap = attributes.
////                stream().
////                collect(Collectors.groupingBy(
////                        AttributeOptionDTO::attributeName,
////                        LinkedHashMap::new,
////                        Collectors.mapping(AttributeOptionDTO::option,Collectors.toSet())
////
////                ));
//
//        Map<String, List<String>> categoryAttributesResponsesMap  = new HashMap<>();
//
//        List<String> attributeNames = attributes.stream().map(AttributeOptionDTO::attributeName).toList();
//
//        categoryAttributesResponsesMap = attributeNames
//                .stream()
//                .collect(Collectors.toMap(attributeName -> attributeName, attributeName ->
//                        attributes.stream().map(AttributeOptionDTO::option).collect(Collectors.toList())
//                        ));
//
//        for (String attributeName: attributeNames) {
//            if (categoryAttributesResponsesMap.containsKey(attributeName)) {
//                categoryAttributesResponsesMap
//                        .get(attributeName)
//                        .add(attributes
//                                .stream()
//                                .filter(attributeOptionDTO -> attributeOptionDTO
//                                        .attributeName().equals(attributeName)).findFirst().get().option());
//            }
//        }
//
//
//        for (Map.Entry<String, Set<String>> entry : categoryAttributesResponsesMap.entrySet()) {
//
//            String measurement = "";
//
//            for (AttributeOptionDTO attributeOptionDTO : attributes) {
//                if (attributeOptionDTO.attributeName().equals(entry.getKey())) {
//                    measurement = attributeOptionDTO.measurementUnit();
//                    break;
//                }
//            }
//
//            categoryAttributesResponses.add(new CategoryAttributesResponse(entry.getKey(),entry.getValue(),measurement));
//        }
//
//        return categoryAttributesResponses;
//    }

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





//    public static AttributeOptionResponse attributeOptionToResponse(CategoryAttribute entity) {
//        AttributeOptionResponse response = new AttributeOptionDTO(
//          entity.getAttributeName().getAttributeName(),
//          entity.getAttributeOption(),
//          entity.get
//        );
//    }
}
