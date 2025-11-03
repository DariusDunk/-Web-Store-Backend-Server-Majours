package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.CategoryAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

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

    public Set<CategoryAttribute> getByNameAndOption(Set<Integer> nameIds, Set<String> options) {
        return categoryAttributeRepository.findByNameIdAndOption(nameIds, options);
    }

}
