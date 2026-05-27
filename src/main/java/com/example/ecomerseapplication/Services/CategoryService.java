package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UpdateCategoryRequest;
import com.example.ecomerseapplication.DTOs.responses.DetailedCategoryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.DetailedAttributeGroupsWithCategoryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
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
    private final AttributeGroupService attributeGroupService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, AttributeGroupService attributeGroupService) {
        this.categoryRepository = categoryRepository;
        this.attributeGroupService = attributeGroupService;
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

        List<Long> attributeGroupIds = attributeGroups.stream()
                .map(AttributeGroupsWithCategoryProjection::getId)
                .toList();
        List<AttributeOfProjection> attributeOfProjections = attributeGroupService.getByGroupId(attributeGroupIds);

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

        List<DetailedAttributeGroupsWithCategoryResponse> groupResponseList = new ArrayList<>();

        for (AttributeGroupsWithCategoryProjection attributeGroup : attributeGroups) {
            List<AttributeOfProjection> attributeProjections = groupAttributesMap
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

