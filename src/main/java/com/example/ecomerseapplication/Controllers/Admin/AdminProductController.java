package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/product/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping("suggestions")
    public ResponseEntity<?> getAllProducts(@RequestParam() String keyword){
        return ResponseEntity.ok(productService.getSuggestionsForSale(keyword));
    }
}
