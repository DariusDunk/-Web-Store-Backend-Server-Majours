package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.ManufacturerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/manufacturer/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminManufacturerController {

    private final ManufacturerService manufacturerService;

    public AdminManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping("all")
    public ResponseEntity<?> getAllManufacturers() {
        return ResponseEntity.ok(manufacturerService.getAllCompact());
    }

}
