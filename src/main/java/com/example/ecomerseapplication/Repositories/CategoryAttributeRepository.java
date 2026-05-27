package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfProjection;
import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Integer>, JpaSpecificationExecutor<CategoryAttribute> {

    List<CategoryAttribute> findByProductCategory(ProductCategory productCategory);

    @Query(
"""
select ca.attributeOption as value,
an.measurementUnit as measurementUnit,
an.attributeName as name,
an.id as nameId
from CategoryAttribute ca
join ca.products p
join ca.attributeName an
where p.id = :productId
order by an.id asc
"""
    )
    List<AttributeOfProjection> findAttributesOfProduct(int productId);


}
