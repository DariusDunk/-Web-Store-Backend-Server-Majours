package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {


    @Query(
"""
update Sale s
set s.isActive = false
where s in ?1
"""
    )
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void markAsInactive(List<Sale> sales);


    @Query(
"""
select s
from Sale s
where s.isActive = true and s.endDate<current_timestamp
"""
    )
    List<Sale> getExpiredSales();

}
