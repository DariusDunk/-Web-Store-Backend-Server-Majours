package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.responses.ProductOfSaleResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactSaleProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.FiltersPriceRange;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {



    @Query(value =
            "select p.productName " +
                    "from Product p " +
                    "where p.productName ilike %?1% " +
                    "order by p.rating desc, p.productName asc, p.id asc " +
                    "limit 7 ")
    List<String> getNameSuggestions(String name);

    Page<Product> getByProductCategory(ProductCategory productCategory, Pageable pageable);

    Optional<Product> getByProductCode(String productCode);

    @EntityGraph(attributePaths = {"saleProducts", "saleProducts.sale"})
    Optional<Product> findProductByProductCode(String productCode);

    @EntityGraph(attributePaths = {"saleProducts", "saleProducts.sale"})
    List<Product> findAllByProductCodeIn(List<String> productCodes);

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


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query(
"""
select p
from Product  p
/*left join p.saleProducts sp on sp.isMain = true
left join sp.sale s on s.isActive = true
and current_timestamp between s.startDate and s.endDate*/
where p.productCode in ?1
"""
    )
    List<Product> getByCodesForPurchaseWithLocking(List<String> productCodes);//todo napravi vtora zaqvka za joinovete v bude6te

    @Query(
"""
select p.productCode as productCode,
p.productName as name,
p.originalPriceStotinki as originalPriceStotinki,
(
p.originalPriceStotinki *
 (100 - coalesce(sp.overrideDiscountPercentage, s.discountPercent, 0)) + 50
) / 100 as discountedPriceStotinki,
p.rating as rating,
p.reviewCount as reviewCount,
p.mainImageUrl as imageUrl,
p.quantityInStock > 0 as isInStock
from Product p
join p.saleProducts sp
join sp.sale s
where s.id = :saleId
  and s.isActive = true
  and current_timestamp between s.startDate and s.endDate
  and sp.isMain = true
  and p.quantityInStock>0
order by coalesce(sp.overrideDiscountPercentage, s.discountPercent, 0) desc,
p.reviewCount desc,
p.rating desc
"""
    )
    List<CompactSaleProductProjection> getTopProductsOfSale(@Param("saleId") long saleId, Pageable pageable);

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
            "AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate ")
    Page<CompactProductDto> findAllAsResponseSortByRating(Pageable pageable);

    @Query(
"""
select p.productCode as productCode,
p.productName as name,
p.originalPriceStotinki as originalPriceStotinki,
s.discountPercent as defaultSaleDiscount,
sp.overrideDiscountPercentage as explicitDiscount,
p.rating as rating,
p.reviewCount as reviewCount,
p.mainImageUrl as imageUrl,
p.quantityInStock>0 as isInStock
from Product p
left join p.saleProducts sp
on sp.isMain = true
left join sp.sale s
  on s.isActive = true
  and current_timestamp between s.startDate and s.endDate
join p.productCategory pc
where p.quantityInStock>0 and pc.id = :categoryId
order by (p.rating * log(p.reviewCount + 1)) desc
"""
    )
    List<CompactProductProjection> getTopProductsOfCategory(@Param("categoryId") int id, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    List<Product> getAllByIdIn(Set<Integer> ids);

    @Query(value =
            "select p.productName as name, p.productCode as productCode " +
                    "from Product p " +
                    "where p.productName ilike %?1% " +
                    "order by p.rating, p.productName " +
                    "limit 7 ")
    List<CompactProductProjection> getNameSuggestionsForSaleForm(String name);

    @Query(
"""
select new com.example.ecomerseapplication.DTOs.responses.ProductOfSaleResponse
(p.productName,
p.productCode,
sp.overrideDiscountPercentage)
from Product p
join p.saleProducts sp
where sp.sale.id = ?1
""")
    List<ProductOfSaleResponse> getAllProductsOfSaleMini(long saleId);

    @Query(
"""
select p.productCode as productCode,
p.productName as name,
p.originalPriceStotinki as originalPriceStotinki,
pc.categoryName as categoryName,
m.manufacturerName as manufacturerName,
p.quantityInStock as quantityInStock,
p.id as id
from Product p
join p.productCategory pc
join p.manufacturer m
"""
    )
    Page<DetailedProductProjection> getAllDetailedProductsPaged(Pageable pageable);

    @Query(
"""
select p.productCode as productCode,
p.productName as name,
p.originalPriceStotinki as originalPriceStotinki,
pc.id as categoryId,
m.id as manufacturerId,
p.quantityInStock as quantityInStock,
p.id as id,
p.model as model,
p.productDescription as productDescription
from Product p
join p.productCategory pc
join p.manufacturer m
where p.id = ?1
"""
    )
    Optional<DetailedProductProjection> getByIdDetProjection(int id);

    @Query(
"""
select p
from Product p
left join fetch p.productImages
where p.id = ?1
"""
    )
    Optional<Product> getByIdWithImages(int productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query(
"""
select p
from Product p
left join fetch p.productImages
where p.id = ?1
"""
    )
    Optional<Product> getByIdWithImagesAndLock(int id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query(
"""
select p
from Product p
left join fetch p.categoryAttributeSet
where p.id =?1
"""
    )
    Optional<Product> getByIdWithAttributesAndLock(int id);

}
