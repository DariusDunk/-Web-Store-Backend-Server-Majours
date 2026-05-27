package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Integer> {

    Optional<ProductCategory> findByCategoryNameAndIsDeleted(String categoryName, Boolean isDeleted);

    @Query(value = "select c.categoryName " +
            "from ProductCategory c " +
            "where c.isDeleted = false " +
            "order by c.categoryName asc")
    List<String> getAllNamesActive();

    @Query(value =
    """
select distinct new com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO(an.attributeName, ca.attributeOption, an.measurementUnit)
from ProductCategory pc
join pc.products p
join p.categoryAttributeSet ca
join ca.attributeName an
join pc.attributeGroups ag
where pc.id=:categoryId
order by an.attributeName asc
    """)
    List<AttributeOptionDTO>getAttributesOfCategory(@Param("categoryId") int productCategoryId);

    @Query(
"""
select
pc.id
from ProductCategory pc
join pc.products p
where pc.isDeleted = false
group by pc
order by sum(p.reviewCount) desc,
avg(p.rating) desc,
count(p.id) desc
"""
            )
    List<Integer> getTopCategoriesIds(Pageable pageable);

    List<ProductCategory> findAllByCategoryNameIn(List<String> categoryNames);

    @Query(
"""
select pc.id as id,
pc.categoryName as name,
pc.isDeleted as isDeleted
from ProductCategory pc
order by pc.isDeleted asc, pc.id asc
""")
    List<CompactAdminCategoryProjection> findAllCompact();

}
