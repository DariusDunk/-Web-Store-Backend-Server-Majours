package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("emails/")
@RequiredArgsConstructor
public class TestMailController {

    private final EmailService emailService;

    @PostMapping("test-email")
    public ResponseEntity<?> testEmail() {

        emailService.sendEmail(
                "test@test.com",
                "Test",
                "MailHog works"
        );

        return ResponseEntity.ok().build();
    }
}
