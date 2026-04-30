package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.SaleProductId;
import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Entities.SaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SaleProductRepository extends JpaRepository<SaleProduct, SaleProductId> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteBySaleIn(List<Sale> sales);
}
