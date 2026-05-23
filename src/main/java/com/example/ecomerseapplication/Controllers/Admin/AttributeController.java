package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/attribute/")
public class AttributeController {

    private final AdminAttributeService adminAttributeService;

    public AttributeController(AdminAttributeService adminAttributeService) {
        this.adminAttributeService = adminAttributeService;
    }

    @GetMapping("attribute-group/all")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> getAllAttributes() {
        return ResponseEntity.ok(adminAttributeService.getAllDetailedAttributeGroups());
    }
}
