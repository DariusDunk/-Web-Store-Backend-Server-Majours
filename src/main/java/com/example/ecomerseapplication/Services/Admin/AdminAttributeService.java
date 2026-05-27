package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.DetailedAttributeGroupResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
import com.example.ecomerseapplication.Mappers.AttributeMapper;
import com.example.ecomerseapplication.Services.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAttributeService {

    private final AttributeGroupService attributeGroupService;
    private final AttributeNameService attributeNameService;
    private final CategoryAttributeService categoryAttributeService;
    private final CategoryService categoryService;
    private final ProductService productService;

    public AdminAttributeService(AttributeGroupService attributeGroupService, AttributeNameService attributeNameService, CategoryAttributeService categoryAttributeService, CategoryService categoryService, ProductService productService) {
        this.attributeGroupService = attributeGroupService;
        this.attributeNameService = attributeNameService;
        this.categoryAttributeService = categoryAttributeService;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    public List<DetailedAttributeGroupResponse> getAllDetailedAttributeGroups() {

        List<AttributeGroup> attributeGroups = attributeGroupService.getAll();

        List<AttributeOfProjection> attributeOfProjections = attributeGroupService.getAllGroupsWithAttributes();

        Map<Long, List<AttributeOfProjection>> groupAttributesMap = new HashMap<>();

        for (AttributeOfProjection attribute : attributeOfProjections) {
            if (groupAttributesMap.isEmpty()
                    || !groupAttributesMap.containsKey(attribute.getGroupId())) {

                groupAttributesMap.put(attribute.getGroupId(),
                        new ArrayList<>(List.of(attribute)));

            } else {
                groupAttributesMap.get(attribute.getGroupId()).add(attribute);
            }
        }
        List<DetailedAttributeGroupResponse> groupResponseList = new ArrayList<>();


        for (AttributeGroup attributeGroup : attributeGroups) {
            List<AttributeOfProjection> attributeProjections = groupAttributesMap
                    .get(attributeGroup.getId());

            List<AttributeOfGroupDTO> attributesDto = new ArrayList<>();
            if (attributeProjections != null && !attributeProjections.isEmpty())
            {
                attributesDto = AttributeMapper
                        .attributeOfGroupProjListToDtoList(attributeProjections);
            }

            DetailedAttributeGroupResponse groupResponse = AttributeMapper
                    .attributeGroupProjToResponse(attributeGroup, attributesDto);

            groupResponseList.add(groupResponse);
        }
        return groupResponseList;

    }

//    List<AttributeOfProductResponse>
//    private List<AttributeOfProductResponse> getAttributesOfProduct(int id) {
//
//        Product product = productService.getById(id);
//
//        List<AttributeOfProjection> projections = categoryAttributeService.getAttributesOfProduct(id);
////
////        Map<Integer, AttributeOfProjection> projectionMap = projections.stream().collect(HashMap::new,
////                (m, e) -> m.put(e.getNameId(), e), HashMap::putAll);
//
//        List<Integer> attributeNameIds = projections.stream().map(AttributeOfProjection::getNameId).toList();
//
////        Map<Integer, String> prodAttrNamemeasurementUnitsMap = categoryService
////                .getSpecificAttributesOfCategoryBetter(product.getProductCategory().getId(), attributeNameIds);
//
//
//
//
//        List<AttributeOfProductResponse> attributeOfProductResponses = new ArrayList<>();
//
//        for (AttributeOfProjection projection : projections) {
//            AttributeOfProductResponse response = new AttributeOfProductResponse(projection.getNameId(),
//                    projection.getName(),
//                    projection.getValue(),
//                    prodAttrNamemeasurementUnitsMap.get(projection.getNameId()),
//                    null);
//            attributeOfProductResponses.add(response);
//        }
//        return attributeOfProductResponses;
//    }

//    public void getAttributesOfProductAndCategory(int id) {
//        Product product = productService.getById(id);
//        ProductCategory category = categoryService.getById(id);
//
//        List<AttributeOfProductResponse> productAttributes = getAttributesOfProduct(id);
//        List<AttributeOfProductResponse> categoryAttributes = attributeOfGroupService.getAllByCategoryProjection(id);
//    }
}
