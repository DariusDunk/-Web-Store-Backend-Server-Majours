package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.responses.CategoryAttributesResponse;
import com.example.ecomerseapplication.DTOs.responses.CategoryFiltersResponse;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.AttributeMapper;
import com.example.ecomerseapplication.Mappers.ManufacturerConverter;
import com.example.ecomerseapplication.Services.AttributeNameService;
import com.example.ecomerseapplication.Services.ManufacturerService;
import com.example.ecomerseapplication.Services.ProductCategoryService;
import com.example.ecomerseapplication.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("category/")
public class CategoryController {


    private final ProductCategoryService categoryService;
    private final ManufacturerService manufacturerService;
    private final ProductService productService;

    @Autowired
    public CategoryController(ProductCategoryService categoryService, AttributeNameService attributeNameService, ManufacturerService manufacturerService, ProductService productService) {
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
        this.productService = productService;
    }

    @GetMapping("names")
    public ResponseEntity<List<String>> getAllNames() {

        List<String> names = categoryService.getAllCategoryNames();

        if (names.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(names);
    }

    @GetMapping("filters")
    public ResponseEntity<?> getAttributes(@RequestParam String categoryName) {
        ProductCategory category = categoryService.findByName(categoryName);

        if (category==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CATEGORY NOT FOUND");

        CategoryFiltersResponse categoryFiltersResponse = new CategoryFiltersResponse();

        categoryFiltersResponse.manufacturerNames = manufacturerService.getNamesByCategory(category);

//        categoryFiltersResponse.manufacturerDTOResponseSet = ManufacturerConverter.objectArrSetToDtoSet(
//                manufacturerService.
//                        getByCategory(category)
//        );

//        if (categoryFiltersResponse.manufacturerDTOResponseSet == null||
//                categoryFiltersResponse.manufacturerDTOResponseSet.isEmpty())
//            return ResponseEntity.notFound().build();

        if (categoryFiltersResponse.manufacturerNames.isEmpty()) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("MANUFACTURERS NOT FOUND");
        }

        Set<CategoryAttributesResponse> attributesResponses = AttributeMapper
                .attributeOptionListToCatAttrResponseSet(categoryService.getAttributesOfCategory(category.getId()));
        if (attributesResponses.isEmpty())
        {
            categoryFiltersResponse.categoryAttributesResponses = new HashSet<>();
        }

        else
            categoryFiltersResponse.categoryAttributesResponses =  attributesResponses;

        categoryFiltersResponse.ratings = productService.getRatingsOfCategory(category);

        Object[] totalPriceRange = productService.getTotalPriceRangeOfCategory(category);

        if (totalPriceRange.length==2)
        {
            categoryFiltersResponse.priceLowest = Integer.parseInt(totalPriceRange[0].toString());
            categoryFiltersResponse.priceHighest = Integer.parseInt(totalPriceRange[1].toString());
        }

        return ResponseEntity.ok(categoryFiltersResponse);
    }
}
