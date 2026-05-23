package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.DetailedAttributeGroupResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.DetailedAttributeGroupsWithCategoryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfGroupProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
import com.example.ecomerseapplication.Mappers.AttributeMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAttributeService {

    private final AttributeOfGroupService attributeOfGroupService;
    private final AttributeGroupService attributeGroupService;
    private final AttributeNameService attributeNameService;
    private final CategoryAttributeService categoryAttributeService;

    public AdminAttributeService(AttributeOfGroupService attributeOfGroupService, AttributeGroupService attributeGroupService, AttributeNameService attributeNameService, CategoryAttributeService categoryAttributeService) {
        this.attributeOfGroupService = attributeOfGroupService;
        this.attributeGroupService = attributeGroupService;
        this.attributeNameService = attributeNameService;
        this.categoryAttributeService = categoryAttributeService;
    }

    public List<DetailedAttributeGroupResponse> getAllDetailedAttributeGroups() {

        List<AttributeGroup> attributeGroups = attributeGroupService.getAll();

        List<AttributeOfGroupProjection> attributeOfGroupProjections = attributeOfGroupService.getAllDetailedProjection();

        Map<Long, List<AttributeOfGroupProjection>> groupAttributesMap = new HashMap<>();

        for (AttributeOfGroupProjection attribute : attributeOfGroupProjections) {
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
            List<AttributeOfGroupProjection> attributeProjections = groupAttributesMap
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
}
