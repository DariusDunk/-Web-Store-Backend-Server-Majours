package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.Admin.AdminProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/product-image/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminProductImageController {

    private final AdminProductImageService adminProductImageService;

    public AdminProductImageController(AdminProductImageService adminProductImageService) {
        this.adminProductImageService = adminProductImageService;
    }

    @GetMapping("of-product/{id}")
    public ResponseEntity<?> getProductImages(@PathVariable Integer id) {
        return ResponseEntity.ok(adminProductImageService.getProductImages(id));
    }
}
