package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}
