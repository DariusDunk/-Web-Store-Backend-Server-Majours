package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Integer> {
    @Query(value = "  select m.id, m.manufacturerName " +
            "from Manufacturer m " +
            "join Product p ON p.manufacturer.id = m.id " +
            "join ProductCategory pc ON pc.id = p.productCategory.id " +
            "where p.productCategory = ?1")
    Set<Object[]> getByCategory(ProductCategory productCategory);


    Optional<Manufacturer> findByManufacturerName(String name);

    List<Manufacturer> findAllByManufacturerNameIn(Collection<String> manufacturerNames);
}
