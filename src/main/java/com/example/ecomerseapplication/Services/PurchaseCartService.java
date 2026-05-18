package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductPairProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductProjection;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Entities.PurchaseCart;
import com.example.ecomerseapplication.Repositories.PurchaseCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PurchaseCartService {


    private final PurchaseCartRepository purchaseCartRepository;

    @Autowired
    public PurchaseCartService(PurchaseCartRepository purchaseCartRepository) {
        this.purchaseCartRepository = purchaseCartRepository;
    }

    @Transactional
     public void saveCarts(List<PurchaseCart> purchaseCarts) {
        purchaseCartRepository.saveAll(purchaseCarts);
    }

    public List<PurchaseCart> getByPurchase(Purchase purchase) {
        return purchaseCartRepository.getByPurchase(purchase);
    }

    public Boolean isProductPurchased(String productCode, String  userId) {
        return purchaseCartRepository.isProductPurchased(productCode, userId);
    }

    public List<PurchaseProductProjection> getByPurchaseCode(String purchaseCode) {
        return purchaseCartRepository.getProductProjectionsOfPurchase(purchaseCode);
    }

    public List<PurchaseProductPairProjection> getProductsForCompactPurchaseHistory(List<Long> purchaseIds) {
        return purchaseCartRepository.getProductsForCompactPurchaseHistory(purchaseIds);
    }
}
