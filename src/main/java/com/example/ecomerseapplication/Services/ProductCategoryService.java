package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAll();
    }

    public List<String> getAllCategoryNames() {
        return productCategoryRepository.getAllNames();
    }

    public Optional<ProductCategory> findById(int id) {
        return productCategoryRepository.findById(id);
    }

    public ProductCategory findByName(String name) {
        return productCategoryRepository.findByCategoryName(name).orElse(null);
    }

    public List<AttributeOptionDTO> getAttributesOfCategory(int categoryId) {
        return productCategoryRepository.getAttributesOfCategory(categoryId);
    }

    public List<String[]> getSpecificAttributesOfCategory(int categoryId,
                                                                         List<AttributeName> attributeNames) {
        List<String> combinations = productCategoryRepository.getMeasurementUnitsOfCategoryAttributes(categoryId, attributeNames);

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
}
