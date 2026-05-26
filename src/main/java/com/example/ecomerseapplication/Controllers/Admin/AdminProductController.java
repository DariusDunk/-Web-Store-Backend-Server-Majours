package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.ProductFormRequest;
import com.example.ecomerseapplication.Services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/product/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("suggestions")
    public ResponseEntity<?> getAllProductsSuggestions(@RequestParam() String keyword) {
        return ResponseEntity.ok(productService.getSuggestionsForSale(keyword));
    }

    @GetMapping("all/p/{page}")
    public ResponseEntity<?> getAllProducts(@PathVariable String page) {
        return ResponseEntity.ok(productService.getAllProductsPaged(page));
    }

    @GetMapping("detail/{id}")
    public ResponseEntity<?> getDetailedProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getByIdForAdminResponse(id));
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody ProductFormRequest request) {

        productService.updateProduct(request, id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("create")
    public ResponseEntity<?> createProduct(@RequestBody ProductFormRequest request) {
        productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
