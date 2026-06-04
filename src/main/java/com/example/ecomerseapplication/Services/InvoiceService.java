package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductPricePairDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.InvoiceFullDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductProjection;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PurchaseService purchaseService;
    private final PurchaseCartService purchaseCartService;
    private final PDFService pdfService;
    private final InvoiceHtmlService invoiceHtmlService;
    private final EmailService emailService;

    public InvoiceFullDTO buildInvoiceForCustomer(String purchaseCode, String customerId) {
        InvoicePurchaseProjection invoice = purchaseService.getInvoiceOfPurchaseForCustomer(purchaseCode, customerId);
        return mainInvoiceBuildAction(purchaseCode, invoice);
    }

    public InvoiceFullDTO buildInvoiceForGuest(String purchaseCode, String sessionId) {
        InvoicePurchaseProjection invoice = purchaseService.getInvoiceOfPurchaseForGuest(purchaseCode, sessionId);
        return mainInvoiceBuildAction(purchaseCode, invoice);
    }

    @NonNull
    private InvoiceFullDTO mainInvoiceBuildAction(String purchaseCode, InvoicePurchaseProjection invoice) {
        List<PurchaseProductProjection> purchaseCarts = purchaseCartService.getByPurchaseCode(purchaseCode);

//        List<CompactProductPricePairDTO> pricePairDTOS = purchaseCarts
//                .stream()
//                .map(ppp -> new CompactProductPricePairDTO(ppp, ppp.getPurchasePrice()))
//                .toList();

//        for (PurchaseProductProjection product : purchaseCarts) {
//            pricePairDTOS.add(new CompactProductPricePairDTO(product, finalPrice));
//        }

        return new InvoiceFullDTO(invoice, purchaseCarts);
    }


    public void sentInvoiceEmailForCustomer(String purchaseCode, Customer customer) {
        InvoiceFullDTO invoiceFullDTO = buildInvoiceForCustomer(purchaseCode, customer.getKeycloakId());
        String email = customer.getEmail();

        mainInvoiceEmailAction(invoiceFullDTO, email);
    }

    public void sentInvoiceEmailForGuest(String purchaseCode, String email, String sessionId) {
        InvoiceFullDTO invoiceFullDTO = buildInvoiceForGuest(purchaseCode, sessionId);

        mainInvoiceEmailAction(invoiceFullDTO, email);
    }

    public byte[] getInvoicePdfForPurchase(String purchaseCode, String keycloakId) {
        InvoiceFullDTO invoiceFullDTO = buildInvoiceForCustomer(purchaseCode, keycloakId);
        String pdfHTML = invoiceHtmlService.buildInvoicePdfString(invoiceFullDTO);
        return pdfService.generateInvoicePdf(pdfHTML);
    }

    private void mainInvoiceEmailAction(InvoiceFullDTO invoiceFullDTO, String email) {
        String pdfHTML = invoiceHtmlService.buildInvoicePdfString(invoiceFullDTO);
        byte[] pdfBytes = pdfService.generateInvoicePdf(pdfHTML);
        String emailHTML = invoiceHtmlService.buildEmailHTML(invoiceFullDTO);

        emailService.sendHTMLEmailWithPDFAttachment(email, "Успешно обработена поръчка в Агромаг",
                emailHTML,
                pdfBytes,
                "invoice.pdf");
    }

}
