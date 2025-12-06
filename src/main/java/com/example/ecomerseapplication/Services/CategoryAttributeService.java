package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.CategoryAttributeRepository;
import com.example.ecomerseapplication.Specifications.AttributeSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryAttributeService {


    private final CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    public CategoryAttributeService(CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryAttributeRepository = categoryAttributeRepository;
    }

    public List<CategoryAttribute> getAll() {
        return categoryAttributeRepository.findAll();
    }

    public List<CategoryAttribute> getByCategory(ProductCategory productCategory) {
        return categoryAttributeRepository.findByProductCategory(productCategory);
    }

    public Set<CategoryAttribute> getByNamesAndOptions(Map<String, List<String>> stringMap) {


        Specification<CategoryAttribute> specification = AttributeSpecifications.getAttributesByNameAndOption(stringMap);

        return new HashSet<>(categoryAttributeRepository.findAll(specification));

    }
}
