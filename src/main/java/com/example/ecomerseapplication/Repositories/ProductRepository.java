package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    @Query("select new com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto (p.productCode," +
            " p.productName, " +
            "p.originalPriceStotinki, " +
//            "p.salePriceStotinki, " +
            "s.discountPercent, " +
            "sp.overrideDiscountPercentage, " +
            "p.rating, SIZE(p.reviews), " +
            "p.mainImageUrl,  " +
            "case when p.quantityInStock>0 then true else false end) " +
            "from Product p " +
            "left join p.saleProducts sp " +
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

    Page<Product> getByManufacturer(Manufacturer manufacturer, Pageable pageable);

//    Page<Product> getByProductCategoryOrderByRatingDesc(ProductCategory productCategory, Pageable pageable);

    Page<Product> getByProductCategory(ProductCategory productCategory, Pageable pageable);

    Optional<Product> getByProductCode(String productCode);

    @Query(value =
            """
                    select distinct (p.rating)
                            from Product p
                                    where p.productCategory = ?1
                    """)
    Optional<Set<Integer>> getRatingsByCategory(ProductCategory productCategory);

    @Query(value = """
                    select MIN (p.salePriceStotinki), MAX (p.salePriceStotinki)
                    from Product p
                    where p.productCategory =?1
            """)
    Object getTotalPriceRange(ProductCategory productCategory);

    List<Product> getAllByProductCodeIn(List<String> productCode);

}
