package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/session/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminSessionController {
    private final SessionService sessionService;

    public AdminSessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("active/get")
    public ResponseEntity<?> getActiveSessions() {
        return ResponseEntity.ok(sessionService.getActiveSessions());
    }

}
