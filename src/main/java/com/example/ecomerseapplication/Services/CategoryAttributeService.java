package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.CategoryAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public Set<CategoryAttribute> getByNamesAndOptions(Map<String, String> stringMap) {

//        System.out.println(stringMap);
        Set<String> names = stringMap.keySet();

        Set<String> options = new HashSet<>(stringMap.values());

//        System.out.println("RESULT: " + result);
        return categoryAttributeRepository.findByNamesAndOptions(names, options);
    }

}
