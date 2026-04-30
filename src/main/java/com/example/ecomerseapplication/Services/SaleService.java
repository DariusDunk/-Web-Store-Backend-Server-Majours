package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Repositories.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    @Autowired
    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<Sale> getExpiredSales() {
        return saleRepository.getExpiredSales();
    }

    public void markAsInActive(List<Sale> expiredSales) {
        saleRepository.markAsInactive(expiredSales);
    }
}
