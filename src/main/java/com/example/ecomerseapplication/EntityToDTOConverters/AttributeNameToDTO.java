package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.responses.CategoryAttributesResponse;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.CategoryAttribute;
import java.util.HashSet;
import java.util.Set;

public class AttributeNameToDTO {

    public static Set<CategoryAttributesResponse> nameSetToResponseSet(Set<AttributeName> attributeNameSet) {

        Set<CategoryAttributesResponse> responseSet = new HashSet<>();

        for (AttributeName attributeName : attributeNameSet) {
            CategoryAttributesResponse categoryAttributesResponse = new CategoryAttributesResponse();

            categoryAttributesResponse.attributeName = attributeName.getAttributeName();
            categoryAttributesResponse.nameId = attributeName.getId();

            for (CategoryAttribute attribute : attributeName.getCategoryAttributeList())
                categoryAttributesResponse.options.add(attribute.getAttributeOption());
            responseSet.add(categoryAttributesResponse);
        }
        return responseSet;
    }
}
