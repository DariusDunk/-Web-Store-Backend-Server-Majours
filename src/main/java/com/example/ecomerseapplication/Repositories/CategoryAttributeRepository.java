package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Integer>, JpaSpecificationExecutor<CategoryAttribute> {
    List<CategoryAttribute> findByProductCategory(ProductCategory productCategory);

//    @Query(value = "select ca " +
//            "from CategoryAttribute ca " +
//            "where ca.attributeName.attributeName in :names and ca.attributeOption in :options")
//    Set<CategoryAttribute> findByNamesAndOptions(@Param("names") Set<String> names, @Param("options") Set<List<String>> options);

}
