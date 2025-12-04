package com.example.ecomerseapplication.Services;

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

    public Boolean isProductPurchased(String productCode, Long userId) {
        return purchaseCartRepository.isProductPurchased(productCode, userId);
    }
}
