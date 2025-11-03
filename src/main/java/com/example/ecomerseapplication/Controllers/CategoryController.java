package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.responses.CategoryFiltersResponse;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.AttributeNameToDTO;
import com.example.ecomerseapplication.Mappers.ManufacturerConverter;
import com.example.ecomerseapplication.Services.AttributeNameService;
import com.example.ecomerseapplication.Services.ManufacturerService;
import com.example.ecomerseapplication.Services.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("category/")
public class CategoryController {

    @Autowired
    ProductCategoryService categoryService;

    @Autowired
    AttributeNameService attributeNameService;

    @Autowired
    ManufacturerService manufacturerService;

    @GetMapping("")
    public List<ProductCategory> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("names")
    public List<String> getAllNames() {
        return categoryService.getAllCategoryNames();
    }

    @GetMapping("filters")
    public CategoryFiltersResponse getAttributes(@RequestParam int id) {
        Optional<ProductCategory> category = categoryService.findById(id);
        Set<AttributeName> attributeNameSet = attributeNameService.getNameSetByCategory(category.orElse(null));

        CategoryFiltersResponse categoryFiltersResponse = new CategoryFiltersResponse();
        categoryFiltersResponse.manufacturerDTOResponseSet = ManufacturerConverter.objectArrSetToDtoSet(
                manufacturerService.
                        getByCategory(category.orElse(null))
        );
        categoryFiltersResponse.categoryAttributesResponses = AttributeNameToDTO.nameSetToResponseSet(attributeNameSet);

        return categoryFiltersResponse;


    }
}
