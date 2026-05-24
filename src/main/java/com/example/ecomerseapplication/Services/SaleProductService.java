package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Entities.SaleProduct;
import com.example.ecomerseapplication.Repositories.SaleProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleProductService {

    private final SaleProductRepository saleProductRepository;

    @Autowired
    public SaleProductService(SaleProductRepository saleProductRepository) {
        this.saleProductRepository = saleProductRepository;
    }

    public void unmarkAsMainBySales(List<Sale> sales) {
        saleProductRepository.unmarkIsMainBySales(sales);
    }


//    public List<SaleProduct> persistAll(List<SaleProduct> saleProductsForInsert) {
//        return saleProductRepository.saveAll(saleProductsForInsert);
//    }

    public void saveAll(List<SaleProduct> saleProducts) {
        saleProductRepository.saveAll(saleProducts);
    }

    public List<SaleProduct> getAllBySaleId(Long id) {
        return saleProductRepository.getAllBySale_Id(id);
    }

    public void deleteAll(List<SaleProduct> saleProductsToRemove) {
        saleProductRepository.deleteAll(saleProductsToRemove);
    }
}
