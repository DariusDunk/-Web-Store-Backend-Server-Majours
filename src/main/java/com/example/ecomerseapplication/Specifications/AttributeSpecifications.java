package com.example.ecomerseapplication.Specifications;

import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeSpecifications {

    public static Specification<CategoryAttribute> filterByAttributes(Map<String, List<String>> filters) {

        return (root, query, cb) -> {

            Join<CategoryAttribute, AttributeName> nameJoin = root.join("attributeName");

            List<Predicate> predicates = new ArrayList<>();

            for (var entry : filters.entrySet()) {

                String attributeName = entry.getKey();
                List<String> options = entry.getValue();

                Predicate p = cb.and(
                        cb.equal(nameJoin.get("attributeName"), attributeName),
                        root.get("attributeOption").in(options)
                );

                predicates.add(p);
            }

            // return ALL CategoryAttributes matching ANY of the filter groups
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }



}
