package com.example.ecomerseapplication.Specifications;

import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Entities.SaleProduct;
import com.example.ecomerseapplication.MetaModels.Product_;
import jakarta.persistence.criteria.*;

public class PriceExpressions {

    public static Expression<Number> finalPrice(
            Root<Product> root,
//            CriteriaQuery<?> query,
            CriteriaBuilder cb
    ) {
        // Join main sale
        Join<Product, SaleProduct> sp = root.join("saleProducts", JoinType.LEFT);
        sp.on(cb.isTrue(sp.get("isMain")));

        Join<SaleProduct, Sale> sale = sp.join("sale", JoinType.LEFT);

        Expression<Number> originalPrice = root.get(Product_.ORIGINAL_PRICE_STOTINKI);

        // --- active sale condition ---
        Predicate saleActive = cb.and(
                cb.isTrue(sale.get("isActive")),
                cb.lessThanOrEqualTo(sale.get("startDate"), cb.currentTimestamp()),
                cb.or(
                        cb.isNull(sale.get("endDate")),
                        cb.greaterThan(sale.get("endDate"), cb.currentTimestamp())
                )
        );

        // --- discount ---
        Expression<Number> discount = cb.coalesce(
                sp.get("overrideDiscountPercentage"),
                sale.get("discountPercent")
        );

        // --- discounted price with rounding ---
        Expression<Number> discounted = cb.quot(
                cb.sum(
                        cb.prod(originalPrice, cb.diff(100, discount)),
                        50
                ),
                100
        );

        // --- final price ---

        return cb.<Number>selectCase()
                .when(saleActive, discounted)
                .otherwise(originalPrice);
    }
}