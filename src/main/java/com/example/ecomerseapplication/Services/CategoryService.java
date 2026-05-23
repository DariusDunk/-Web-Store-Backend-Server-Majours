package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CompactAdminCategoryProjection> findAllCompact() {
        return categoryRepository.findAllCompact();
    }

    public List<String> getAllCategoryNames() {
        List<String> names = categoryRepository.getAllNames();
        if (names.isEmpty())
            throw new ResourceNotFoundException("No categories found");
        return names;
    }

    public ProductCategory findById(int id) {
        return categoryRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Product Category not found with id: " + id));
    }

    public ProductCategory findByName(String name) {
        return categoryRepository.findByCategoryName(name).orElseThrow(()->new ResourceNotFoundException("Product Category not found with name: " + name));
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
}
