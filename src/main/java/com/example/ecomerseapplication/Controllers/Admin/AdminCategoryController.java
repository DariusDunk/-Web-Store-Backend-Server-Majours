package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.UpdateCategoryRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactCategoryAdminResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;
import com.example.ecomerseapplication.Mappers.CategoryMapper;
import com.example.ecomerseapplication.Services.AttributeNameService;
import com.example.ecomerseapplication.Services.CategoryAttributeService;
import com.example.ecomerseapplication.Services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/category/")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final AttributeNameService attributeNameService;
    private final CategoryAttributeService categoryAttributeService;

    public AdminCategoryController(CategoryService categoryService, AttributeNameService attributeNameService, CategoryAttributeService categoryAttributeService) {
        this.categoryService = categoryService;
        this.attributeNameService = attributeNameService;
        this.categoryAttributeService = categoryAttributeService;
    }


    @GetMapping("all/compact")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> getAllCategories() {
        List<CompactAdminCategoryProjection> projections = categoryService.findAllCompact();

        List<CompactCategoryAdminResponse> response = CategoryMapper.compactAdminProjListToResponseList(projections);

        return ResponseEntity.ok(response);
    }

    @GetMapping("detailed/{categoryId}")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> getDetailedCategory(@PathVariable Integer categoryId) {

        return ResponseEntity.ok(categoryService.getDetailedCategory(categoryId));
    }
//
//    @PostMapping("update")
//    @PreAuthorize("hasRole(@roles.admin())")
//    public ResponseEntity<?> updateCategory(@RequestBody UpdateCategoryRequest request) {}

}
