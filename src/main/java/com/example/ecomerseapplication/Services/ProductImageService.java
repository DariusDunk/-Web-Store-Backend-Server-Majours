package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Repositories.ProductImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductImageService {
    private final ProductImageRepository productImageRepository;

    public ProductImageService(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }


    public List<String> getImageNamesByProductId(int id) {
        return productImageRepository.getImageNamesByProductId(id);
    }
}
