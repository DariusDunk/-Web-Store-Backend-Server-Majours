package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    @Query(value =
            "select p " +
                    "from Product p " +
                    "where p.productName ilike %:name% " +
                    "order by p.rating, p.id ")
    Page<Product> getByNameLike(@Param("name") String name, Pageable pageable);

    @Query("select new com.example.ecomerseapplication.DTOs.responses.CompactProductResponse (p.productCode, p.productName, p.originalPriceStotinki, p.salePriceStotinki, p.rating, SIZE(p.reviews) , p.mainImageUrl) " +
            "from Product p " +
            "order by p.rating desc ")
    Page<CompactProductResponse> findAllAsResponseSortByRating(Pageable pageable);

    @Query(value = "select p " +
            "from Product p " +
            "order by p.rating desc ")
    Page<Product> findAllSortByRating(Pageable pageable);


    @Query(value =
            "select p.productName " +
            "from Product p " +
            "where p.productName ilike %?1% " +
            "order by p.rating, p.productName " +
            "limit 7 ")
    List<String> getNameSuggestions(String name);

        @Query(value = "select p " +
            "from Product p " +
            "order by p.rating desc " +
            "limit 10")
    List<Product> getProductsByRating();

    Page<Product> getByManufacturerOrderByRatingDescIdAsc(Manufacturer manufacturer, Pageable pageable);

    Page<Product> getByProductCategoryOrderByRatingDesc(ProductCategory productCategory, Pageable pageable);

    Optional<Product> getByProductCode(String productCode);
}
