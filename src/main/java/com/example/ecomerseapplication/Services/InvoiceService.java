package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductPricePairDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.InvoiceFullDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductProjection;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PurchaseService purchaseService;
    private final PurchaseCartService purchaseCartService;

    public InvoiceFullDTO buildInvoice(String purchaseCode, String customerId) {
        InvoicePurchaseProjection invoice = purchaseService.getInvoiceOfPurchase(purchaseCode, customerId);
        List<PurchaseProductProjection> purchaseCarts = purchaseCartService.getByPurchaseCode(purchaseCode);
        List<CompactProductPricePairDTO> pricePairDTOS = new ArrayList<>();

        for (PurchaseProductProjection product : purchaseCarts) {
            int originalPrice = product.getOriginalPriceStotinki();
            Short saleDiscount = product.getDefaultSaleDiscount();
            Short explicitDiscount = product.getExplicitDiscount();
            int finalPrice = ProductDTOMapper.calculateDiscountPrice(originalPrice, saleDiscount, explicitDiscount);
            pricePairDTOS.add(new CompactProductPricePairDTO(product, finalPrice));
        }

        return new InvoiceFullDTO(invoice, pricePairDTOS);
    }

}
