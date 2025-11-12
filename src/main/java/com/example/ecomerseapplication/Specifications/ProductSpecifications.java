package com.example.ecomerseapplication.Specifications;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.MetaModels.Product_;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ProductSpecifications {

    public static Specification<Product> equalsManufacturer(Manufacturer manufacturer) {
        if (manufacturer == null) {
            return null;
        }

        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(Product_.MANUFACTURER), manufacturer));
    }

    public static Specification<Product> manufacturerIn(List<Manufacturer> manufacturers) {
        if (manufacturers == null) {
            return null;
        }

        return ((root, query, criteriaBuilder) ->
        {
            assert query != null;
            query.distinct(true);
            Join<Product, Manufacturer> join = root.join(Product_.MANUFACTURER);
            return join.in(manufacturers);
        });
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

    public static Specification<Product> containsAttributes(Set<CategoryAttribute> attributes) {

        return ((root, query, criteriaBuilder) ->
        {
            assert query != null;
            query.distinct(true);
            Join<Product, Set<CategoryAttribute>> join = root.join(Product_.CATEGORY_ATTRIBUTE_SET);
            return join.in(attributes);
        }
        );
    }

    public static Specification<Product> ratingIn(List<Integer> ratings) {
        return ((root, query, criteriaBuilder) ->
                (ratings == null || ratings.isEmpty())
                        ? criteriaBuilder.conjunction()
                        : root.get(Product_.RATING).in(ratings));
    }
}
