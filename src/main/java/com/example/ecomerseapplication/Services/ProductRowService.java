package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.ProductRowResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.VeryCompactSaleProjection;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductRowService {

    private final SaleService saleService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductRowService(SaleService saleService, ProductService productService, CategoryService categoryService) {
        this.saleService = saleService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    public List<ProductRowResponse> getTopActiveSaleProducts() {
        List<VeryCompactSaleProjection> saleProjections = saleService.getTopCurrentSalesCompact();

        if (saleProjections.isEmpty()) {
            return List.of();
        }

        List<ProductRowResponse> productRowResponses = new ArrayList<>();

        for (VeryCompactSaleProjection saleProjection : saleProjections) {

            long saleId = saleProjection.getId();

            List<CompactProductResponse> productsOfSale = productService.getTopProductsOfSale(saleId);

            if (productsOfSale.isEmpty()) {
                continue;
            }
            String saleName = saleProjection.getName();
            productRowResponses
                    .add(new ProductRowResponse(GlobalConstants.PRODUCT_ROW_TYPE_SALE,
                            saleName,
                            productsOfSale));

        }

        return productRowResponses;
    }

    public List<ProductRowResponse> getTopCategoryProducts() {
        List<Integer> categoryProjections = categoryService.getTopCategories();

        if (categoryProjections.isEmpty()) {
            return List.of();
        }

        List<ProductRowResponse> productRowResponses = new ArrayList<>();

        for (Integer categoryId : categoryProjections) {

            ProductCategory category = categoryService.findById(categoryId);
            String name = category.getCategoryName();
            List<CompactProductProjection> products = productService.getTopProductsOfCategory(category);

            List<CompactProductResponse> productResponses = ProductDTOMapper.compactProjectionListToResponseList(products);

            if (products.isEmpty()) {
                continue;
            }

            productRowResponses.add(new ProductRowResponse(GlobalConstants.PRODUCT_ROW_TYPE_CATEGORY, name, productResponses));
        }
        return productRowResponses;
    }
}
