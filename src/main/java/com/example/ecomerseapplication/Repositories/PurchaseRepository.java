package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;
import com.example.ecomerseapplication.Entities.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Long> {

    @Query("""
        select p
                from Purchase p
                        where p.customer.keycloakId= :keycloakId
        """
            )
    List<Purchase> getByCustomer(@Param("keycloakId")String  customer);

    @Query(
"""
select p.purchaseCode as purchaseCode,
p.date as date,
p.totalCost as totalCost,
p.shippingFee as shippingFee,
p.productTotal as productTotal,
p.deliveryStatus as deliveryStatus,
p.paymentMethod as paymentMethod,
p.contactName as contactName,
p.address as address,
p.email as email
from Purchase p
where p.customer.keycloakId = :customerId and p.purchaseCode = :purchaseCode
"""
    )
    Optional<InvoicePurchaseProjection> getByCodeAndCustomerId(@Param("customerId")String customerKeycloakId, @Param("purchaseCode")String purchaseCode);


    @Query(
"""
select p.purchaseCode as purchaseCode,
p.date as date,
p.totalCost as totalCost,
p.shippingFee as shippingFee,
p.productTotal as productTotal,
p.deliveryStatus as deliveryStatus,
p.paymentMethod as paymentMethod,
p.contactName as contactName,
p.address as address,
p.email as email
from Purchase p
where p.session.sessionId = :sessionId
and p.purchaseCode = :purchaseCode
"""
    )
    Optional<InvoicePurchaseProjection> getByCodeAndSessionId(@Param("sessionId")String sessionId, @Param("purchaseCode")String purchaseCode);

}
