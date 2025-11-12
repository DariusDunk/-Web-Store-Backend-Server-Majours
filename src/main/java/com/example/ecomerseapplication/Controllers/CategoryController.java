package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.responses.CategoryFiltersResponse;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.AttributeNameToDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("category/")
public class CategoryController {


    private final ProductCategoryService categoryService;

    private final AttributeNameService attributeNameService;

    private final ManufacturerService manufacturerService;
    private final ProductService productService;

    @Autowired
    public CategoryController(ProductCategoryService categoryService, AttributeNameService attributeNameService, ManufacturerService manufacturerService, ProductService productService) {
        this.categoryService = categoryService;
        this.attributeNameService = attributeNameService;
        this.manufacturerService = manufacturerService;
        this.productService = productService;
    }

//    @GetMapping("")
//    public List<ProductCategory> getAll() {
//        return categoryService.findAll();
//    }

    @GetMapping("names")
    public ResponseEntity<List<String>> getAllNames() {

        List<String> names = categoryService.getAllCategoryNames();

        if (names.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(names);
    }

    @GetMapping("filters")
    public ResponseEntity<?> getAttributes(@RequestParam String categoryName) {
        ProductCategory category = categoryService.findByName(categoryName);

//        System.out.println("inside filters endpoint");

        if (category==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CATEGORY NOT FOUND");

        Set<AttributeName> attributeNameSet = attributeNameService.getNameSetByCategory(category);

        CategoryFiltersResponse categoryFiltersResponse = new CategoryFiltersResponse();
        categoryFiltersResponse.manufacturerDTOResponseSet = ManufacturerConverter.objectArrSetToDtoSet(
                manufacturerService.
                        getByCategory(category)
        );

        if (categoryFiltersResponse.manufacturerDTOResponseSet == null||
                categoryFiltersResponse.manufacturerDTOResponseSet.isEmpty())
            return ResponseEntity.notFound().build();

        if (!attributeNameSet.isEmpty())
        {
            categoryFiltersResponse.categoryAttributesResponses = AttributeNameToDTO.nameSetToResponseSet(attributeNameSet);
        }

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
