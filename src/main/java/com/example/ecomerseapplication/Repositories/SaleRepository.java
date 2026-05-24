package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactSaleProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedSaleProjection;
import com.example.ecomerseapplication.Entities.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(
"""
select s.id as id, s.name as name
from Sale s
where s.isActive = true
and current_timestamp between s.startDate and s.endDate
order by s.discountPercent desc, s.startDate desc
"""
    )
    List<CompactSaleProjection> findActiveAndNotExpired(Pageable pageable);


    @Query(
"""
select s.name as name,
s.discountPercent as defaultDiscount,
s.startDate as startDate,
s.endDate as endDate,
s.isActive as isActive,
(select count (sp)
from SaleProduct sp
where sp.sale.id = s.id
) as productCount
from Sale s
"""
    )
    Page<DetailedSaleProjection> getAllSalesProjection(Pageable pageable);
}
