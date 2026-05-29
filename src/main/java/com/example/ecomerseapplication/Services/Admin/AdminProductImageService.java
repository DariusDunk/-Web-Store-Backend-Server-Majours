package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.ProductImageResponse;
import com.example.ecomerseapplication.DTOs.responses.ProductImagesSectionResponse;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Repositories.ProductImageRepository;
import com.example.ecomerseapplication.Services.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminProductImageService {

    private final ProductService productService;
    private final ProductImageRepository productImageRepository;

    public AdminProductImageService(ProductService productService, ProductImageRepository productImageRepository) {
        this.productService = productService;
        this.productImageRepository = productImageRepository;
    }

    public ProductImagesSectionResponse getProductImages(Integer productId) {
        Product product = productService.getByIdWithImages(productId);

        ProductImageResponse mainImageResponse = new ProductImageResponse(product.getMainImageUrl(),
                product.getProductCode());
        List<ProductImageResponse> productImagesResponse = product
                .getProductImages()
                .stream()
                .map(pr -> new ProductImageResponse(pr.getImageFileName(), product.getProductCode()))
                .toList();

        return new ProductImagesSectionResponse(mainImageResponse, productImagesResponse);
    }

}
