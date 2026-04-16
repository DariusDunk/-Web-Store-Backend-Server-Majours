package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Repositories.SaleProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaleProductService {

    private final SaleProductRepository saleProductRepository;

    @Autowired
    public SaleProductService(SaleProductRepository saleProductRepository) {
        this.saleProductRepository = saleProductRepository;
    }
}
