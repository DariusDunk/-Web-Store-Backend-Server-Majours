package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UpdateCategoryRequest;
import com.example.ecomerseapplication.DTOs.responses.DetailedCategoryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.DetailedAttributeGroupsWithCategoryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfGroupProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.AttributeMapper;
import com.example.ecomerseapplication.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryAttributeService categoryAttributeService;
    private final AttributeGroupService attributeGroupService;
    private final AttributeOfGroupService attributeOfGroupService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CategoryAttributeService categoryAttributeService, AttributeGroupService attributeGroupService, AttributeOfGroupService attributeOfGroupService) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeService = categoryAttributeService;
        this.attributeGroupService = attributeGroupService;
        this.attributeOfGroupService = attributeOfGroupService;
    }

    public List<CompactAdminCategoryProjection> findAllCompact() {
        return categoryRepository.findAllCompact();
    }

    public List<String> getAllCategoryNames() {
        List<String> names = categoryRepository.getAllNamesActive();
        if (names.isEmpty())
            throw new ResourceNotFoundException("No categories found");
        return names;
    }

    public ProductCategory findById(int id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product Category not found with id: " + id));
    }

    public ProductCategory findByNameActive(String name) {
        return categoryRepository.findByCategoryNameAndIsDeleted(name, false).orElseThrow(() -> new ResourceNotFoundException("Product Category not found with name: " + name));
    }

    public List<AttributeOptionDTO> getAttributesOfCategory(int categoryId) {
        return categoryRepository.getAttributesOfCategory(categoryId);
    }

    public List<String[]> getSpecificAttributesOfCategory(int categoryId,
                                                          List<AttributeName> attributeNames) {
        List<String> combinations = categoryRepository.getMeasurementUnitsOfCategoryAttributes(categoryId, attributeNames);

        if (combinations.isEmpty()) {
            return new ArrayList<>();
        }

        List<String[]> attributeOptionResponses = new ArrayList<>();

        for (String combination : combinations) {
            String[] split = combination.split(",");

            if (split.length == 2) {
                attributeOptionResponses.add(split);
            }
        }

        return attributeOptionResponses;
    }


    public List<Integer> getTopCategories() {
        return categoryRepository.getTopCategoriesIds(PageRequest.of(0, 6));
    }

    public List<ProductCategory> getAllByNames(List<String> names) {
        return categoryRepository.findAllByCategoryNameIn(names);
    }


    @Transactional
    public DetailedCategoryResponse getDetailedCategory(Integer categoryId) {
        ProductCategory category = findById(categoryId);
        List<AttributeGroupsWithCategoryProjection> attributeGroups = attributeGroupService.getAllWithACategory(categoryId);
//
//        for (AttributeGroupsWithCategoryProjection attributeGroup : attributeGroups) {
//            System.out.println(attributeGroup);
//        }

        List<Long> attributeGroupIds = attributeGroups.stream()
                .map(AttributeGroupsWithCategoryProjection::getId)
                .toList();
        List<AttributeOfGroupProjection> attributeOfGroupProjections = attributeOfGroupService.getByGroupId(attributeGroupIds);

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

        List<DetailedAttributeGroupsWithCategoryResponse> groupResponseList = new ArrayList<>();

        for (AttributeGroupsWithCategoryProjection attributeGroup : attributeGroups) {
            List<AttributeOfGroupProjection> attributeProjections = groupAttributesMap
                    .get(attributeGroup.getId());

            List<AttributeOfGroupDTO> attributesDto = new ArrayList<>();
            if (attributeProjections != null && !attributeProjections.isEmpty())
            {
                attributesDto = AttributeMapper
                        .attributeOfGroupProjListToDtoList(attributeProjections);
            }

            DetailedAttributeGroupsWithCategoryResponse groupResponse = AttributeMapper
                    .attributeGroupWCatProjToResponse(attributeGroup, attributesDto);

            groupResponseList.add(groupResponse);
        }

        return new DetailedCategoryResponse(category.getCategoryName(), category.getIsDeleted(), groupResponseList);
    }

    @Transactional
    public void updateCategory(UpdateCategoryRequest request) {
        ProductCategory category = findById(request.id());
        List<AttributeGroup> attributeGroups = attributeGroupService.getByNames(request.attributeGroups());

        category.updateCategory(request.name(), attributeGroups, request.isDeleted());
    }

    public void createCategory(UpdateCategoryRequest request) {
        ProductCategory category = new ProductCategory();
        List<AttributeGroup> attributeGroups = attributeGroupService.getByNames(request.attributeGroups());

        category.updateCategory(request.name(), attributeGroups, request.isDeleted());
        categoryRepository.save(category);
    }

    public ProductCategory getById(Integer integer) {
        return categoryRepository.findById(integer)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + integer));
    }
}

