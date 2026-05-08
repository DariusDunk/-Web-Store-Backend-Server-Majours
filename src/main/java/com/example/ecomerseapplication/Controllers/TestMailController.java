package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.serverDtos.InvoiceFullDTO;
import com.example.ecomerseapplication.Services.EmailService;
import com.example.ecomerseapplication.Services.InvoiceService;
import com.example.ecomerseapplication.Services.PDFService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("emails/")
@RequiredArgsConstructor
public class TestMailController {

    private final EmailService emailService;
    private final PDFService pdfService;
    private final InvoiceService invoiceService;

    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail() {

        emailService.sendEmail(
                "test@test.com",
                "Test",
                "MailHog works"
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-pdf-email")
    public ResponseEntity<?> testPdfEmail() {

        byte[] pdf =
                pdfService.generateTestPdf();

        emailService.sendEmailWithPDFAttachment(
                "test@test.com",
                "PDF Test",
                "PDF attachment works.",
                pdf,
                "invoice"
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-pdf-invoice")
    public ResponseEntity<?> testPdfInvoice(@RequestParam String customerId, @RequestParam String purchaseCode) {
        InvoiceFullDTO invoiceFullDTO = invoiceService.buildInvoice(purchaseCode, customerId);
        byte[] pdf = pdfService.generateInvoicePdf(invoiceFullDTO);
        emailService.sendEmailWithPDFAttachment( "test@test.com",
                "PDF Test",
                "PDF attachment for invoice.",
                pdf,
                "invoice");
        return ResponseEntity.ok().build();
    }
}
