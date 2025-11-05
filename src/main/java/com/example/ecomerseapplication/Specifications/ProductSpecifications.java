package com.example.ecomerseapplication.Specifications;

import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.MetaModels.Product_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ProductSpecifications {

    public static Specification<Product> equalsManufacturer(Manufacturer manufacturer) {
        if (manufacturer == null) {
            return null;
        }

        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(Product_.MANUFACTURER), manufacturer));
    }

    public static Specification<Product> priceBetween(int priceLowest, int priceHighest) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get(Product_.SALE_PRICE_STOTINKI),
                priceLowest,
                priceHighest)
        );
    }

    public static Specification<Product> equalsCategory(ProductCategory productCategory) {

        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(Product_.PRODUCT_CATEGORY), productCategory));
    }
}
