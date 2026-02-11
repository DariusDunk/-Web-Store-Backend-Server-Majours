package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Integer>, JpaSpecificationExecutor<CategoryAttribute> {

    List<CategoryAttribute> findByProductCategory(ProductCategory productCategory);
}
