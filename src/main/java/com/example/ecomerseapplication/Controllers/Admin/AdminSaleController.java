package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/sale/")
public class AdminSaleController {

    private final SaleService saleService;

    public AdminSaleController(SaleService saleService) {
        this.saleService = saleService;
    }


    @GetMapping("all/{page}")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> getAllSales(@PathVariable int page) {
        return ResponseEntity.ok(saleService.getAllSales(page));
    }
}
