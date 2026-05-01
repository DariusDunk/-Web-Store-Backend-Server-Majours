package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.FiltersPriceRange;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    @Query("select new com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto (p.productCode," +
            " p.productName, " +
            "p.originalPriceStotinki, " +
            "s.discountPercent, " +
            "sp.overrideDiscountPercentage, " +
            "p.rating, SIZE(p.reviews), " +
            "p.mainImageUrl,  " +
            "case when p.quantityInStock>0 then true else false end) " +
            "from Product p " +
            "left join p.saleProducts sp " +
            "with sp.isMain = true " +
            "left join sp.sale s " +
            "with s.isActive = true " +
            "AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate")
    Page<CompactProductDto> findAllAsResponseSortByRating(Pageable pageable);

    @Query(value =
            "select p.productName " +
                    "from Product p " +
                    "where p.productName ilike %?1% " +
                    "order by p.rating, p.productName " +
                    "limit 7 ")
    List<String> getNameSuggestions(String name);

    @EntityGraph(attributePaths = {"saleProducts", "saleProducts.sale"})
    Page<Product> getByManufacturer(Manufacturer manufacturer, Pageable pageable);

    Page<Product> getByProductCategory(ProductCategory productCategory, Pageable pageable);

    Optional<Product> getByProductCode(String productCode);

    @EntityGraph(attributePaths = {"saleProducts", "saleProducts.sale"})
    Optional<Product> findProductByProductCode(String productCode);

    @Query(value =
            """
                    select distinct (p.rating)
                            from Product p
                                    where p.productCategory = ?1
                    """)
    Optional<Set<Integer>> getRatingsByCategory(ProductCategory productCategory);

    @Query(
"""
select
    MIN(
    (p.originalPriceStotinki * (100 - CASE
        WHEN (s.startDate <= CURRENT_TIMESTAMP AND (s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP))
        THEN COALESCE(sp.overrideDiscountPercentage, s.discountPercent, 0)
        ELSE 0
    END) + 50) / 100
    ) as priceLowest,
    MAX(
                (p.originalPriceStotinki * (100 - CASE
                    WHEN (s.startDate <= CURRENT_TIMESTAMP AND (s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP))
                    THEN COALESCE(sp.overrideDiscountPercentage, s.discountPercent, 0)
                    ELSE 0
                END) + 50) / 100
            ) as priceHighest
from Product p
left join p.saleProducts sp
on sp.isMain = true
left join sp.sale s
where p.productCategory = ?1
"""
    )
    FiltersPriceRange getCategoryPriceRange(ProductCategory productCategory);

    List<Product> getAllByProductCodeIn(List<String> productCode);

}
