package com.example.ecomerseapplication.Specifications;

import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.CategoryAttribute;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeSpecifications {

    public static Specification<CategoryAttribute> getAttributesByNameAndOption(Map<String, List<String>> options) {


        return ((root, criteriaQuery, cb)
                -> {
           List<Predicate> predicates = new ArrayList<>();

            Join<CategoryAttribute, AttributeName>  attrNameJoin = root.join("attributeName");

            for (var entry : options.entrySet()) {
                String attributeName = entry.getKey();
                List<String> values = entry.getValue();

                Predicate predicate = cb.and(
                        cb.equal(attrNameJoin.get("attributeName"),attributeName),
                        root.get("attributeOption").in(values)
                        );
                predicates.add(predicate);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }

        );
    }

}
