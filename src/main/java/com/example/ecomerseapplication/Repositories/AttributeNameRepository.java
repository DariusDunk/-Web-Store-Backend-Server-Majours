package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface AttributeNameRepository extends JpaRepository<AttributeName, Integer> {

    @Query(value = "select an " +
            "from AttributeName an " +
            "where an.id in (select ca.attributeName.id " +
            "from CategoryAttribute ca where ca.productCategory =:category)")
    Set<AttributeName> getByCategory(@Param("category")ProductCategory productCategory);

    @Query(
"""
select an
from AttributeName an
join fetch an.categoryAttributeList
where an.id in ?1
"""
    )
    List<AttributeName> getAllByIdInWithOptions(Collection<Integer> ids);

    Collection<Integer> id(int id);
}
