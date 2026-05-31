package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    @Query(
"""
select pi.imageFileName
from ProductImage pi
where pi.product.id=:id
"""
    )
    List<String> getImageNamesByProductId(int id);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProductImage pi " +
            "WHERE pi.product = :product AND pi.imageFileName IN :names")
    void deleteByProductAndImageFileNameIn(
            @Param("product") Product product,
            @Param("names") List<String> names
    );

    List<ProductImage> getAllByProduct_Id(int productId);
}
