package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.DateRangeRequest;
import com.example.ecomerseapplication.DTOs.requests.PurchaseActionRequest;
import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import com.example.ecomerseapplication.Services.Admin.AdminPurchaseService;
import com.example.ecomerseapplication.Services.ReportPdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/purchase/")
@PreAuthorize("hasRole(@roles.admin())")
@Validated
public class AdminPurchaseController {

    private final AdminPurchaseService adminPurchaseService;
    private final ReportPdfService reportPdfService;

    public AdminPurchaseController(AdminPurchaseService adminPurchaseService, ReportPdfService reportPdfService) {
        this.adminPurchaseService = adminPurchaseService;
        this.reportPdfService = reportPdfService;
    }

    @GetMapping("all/{page}")
    public ResponseEntity<?> getAllPurchases(@PathVariable int page) {
        return ResponseEntity.ok(adminPurchaseService.getPagedPurchasesCompact(page));
    }

    @GetMapping("pending-refund-count")
    public ResponseEntity<?> getRefundPendingCount() {
        return ResponseEntity.ok(adminPurchaseService.getRefundPendingCount());
    }

    @PatchMapping("purchase-action")
    public ResponseEntity<?> performPurchaseAction(@RequestBody PurchaseActionRequest request) {

        adminPurchaseService.executePurchaseStatusAction(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("revenue-report")
    public ResponseEntity<?> getRevenueReportForPeriod(@RequestBody @Valid DateRangeRequest request) {

        return ResponseEntity.ok(adminPurchaseService.revenueReport(request));
    }

    @PostMapping("revenue-report/pdf")
    public ResponseEntity<?> getRevenueReportForPeriodAsPdf(@RequestBody @Valid DateRangeRequest request) {
        ReportResponses.ReportResponse revenueResponse = adminPurchaseService.revenueReport(request);

        byte[] pdfBytes = reportPdfService.generateReportPdf(revenueResponse);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=revenue-report.pdf")
                .body(pdfBytes);
    }
}
