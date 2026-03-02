package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.responses.CategoryAttributesResponse;
import com.example.ecomerseapplication.DTOs.responses.CategoryFiltersResponse;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.AttributeMapper;
import com.example.ecomerseapplication.Services.ManufacturerService;
import com.example.ecomerseapplication.Services.ProductCategoryService;
import com.example.ecomerseapplication.Services.ProductService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Validated
@RequestMapping("category/")
public class CategoryController {


    private final ProductCategoryService categoryService;
    private final ManufacturerService manufacturerService;
    private final ProductService productService;

    @Autowired
    public CategoryController(ProductCategoryService categoryService, ManufacturerService manufacturerService, ProductService productService) {
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
        this.productService = productService;
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @GetMapping("names")
    public ResponseEntity<List<String>> getAllNames() {

        List<String> names = categoryService.getAllCategoryNames();

        return ResponseEntity.ok(names);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @GetMapping("filters")
    public ResponseEntity<?> getAttributes(@RequestParam @NotBlank String categoryName) {

        ProductCategory category = categoryService.findByName(categoryName);

        CategoryFiltersResponse categoryFiltersResponse = new CategoryFiltersResponse();

        categoryFiltersResponse.manufacturerNames = manufacturerService.getNamesByCategory(category);

        List<CategoryAttributesResponse> attributesResponses = AttributeMapper
                .attributeOptionListToCatAttrResponseList(categoryService.getAttributesOfCategory(category.getId()));
        if (attributesResponses.isEmpty())//TODO Tova dali moje da ostane taka se 4udq
        {
            categoryFiltersResponse.categoryAttributesResponses = new ArrayList<>();
        }

        else
            categoryFiltersResponse.categoryAttributesResponses =  attributesResponses;

        categoryFiltersResponse.ratings = productService.getRatingsOfCategory(category);

        Object[] totalPriceRange = productService.getTotalPriceRangeOfCategory(category);

        categoryFiltersResponse.priceLowest = Integer.parseInt(totalPriceRange[0].toString());
        categoryFiltersResponse.priceHighest = Integer.parseInt(totalPriceRange[1].toString());

//        System.out.println("Filters: " + categoryFiltersResponse);

        return ResponseEntity.ok(categoryFiltersResponse);
    }
}
