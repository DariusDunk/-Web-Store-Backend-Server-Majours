package com.example.ecomerseapplication.Specifications;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.MetaModels.Product_;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductSpecifications {

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
        return (root, query, cb) -> {

            if (attributes == null || attributes.isEmpty()) {
                return cb.conjunction();
            }

            assert query != null;
            query.distinct(true);

            // Group CategoryAttributes by AttributeName
            Map<Integer, Set<Integer>> groups = attributes.stream()
                    .collect(Collectors.groupingBy(
                            a -> a.getAttributeName().getId(), // group by name ID
                            Collectors.mapping(CategoryAttribute::getId, Collectors.toSet()) // collect option IDs
                    ));

            List<Predicate> groupPredicates = new ArrayList<>();

            // AND between attribute groups
            for (var entry : groups.entrySet()) {

                Set<Integer> optionIds = entry.getValue();

                Join<Product, CategoryAttribute> join =
                        root.join(Product_.CATEGORY_ATTRIBUTE_SET, JoinType.INNER);

                // OR within group (one must match)
                Predicate groupOr = join.get("id").in(optionIds);

                groupPredicates.add(groupOr);
            }

            // Final: AND all groups
            return cb.and(groupPredicates.toArray(new Predicate[0]));
        };
    }


    public static Specification<Product> ratingEqualOrHigher(Integer rating) {
        return ((root, query, criteriaBuilder) ->
        {
            if (rating == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get(Product_.RATING), rating);
        });
    }
}
