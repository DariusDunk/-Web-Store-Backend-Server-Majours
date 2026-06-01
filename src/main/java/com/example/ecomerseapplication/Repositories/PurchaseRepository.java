package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AdditionalPurchaseDataProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProjection;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.enums.DeliveryStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
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
    Optional<InvoicePurchaseProjection> getInvoiceDataByCodeAndCustomerId(@Param("customerId")String customerKeycloakId, @Param("purchaseCode")String purchaseCode);


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

    int countByCustomer_KeycloakId(String customerKeycloakId);

    Page<Purchase> getPurchasesByCustomer_KeycloakId(String customerKeycloakId, Pageable pageable);

    @Query(
"""
select
p.productTotal as productTotal,
p.contactName as recipientName,
p.paymentMethod as paymentMethod,
p.contactNumber as recipientPhone,
p.deliveryDate as deliveryDate
from Purchase p
join p.customer c
where p.purchaseCode = :purchaseCode and c.keycloakId = :customerId
"""
    )
    AdditionalPurchaseDataProjection getAdditionalPurchaseDataByPurchaseCodeAndCustomer(@Param("purchaseCode") String purchaseCode,
                                                                                        @Param("customerId")String customerId);

    /**
     * Retrieves a purchase by customer Keycloak ID and purchase code with a pessimistic write lock.
     * <p>
     * <strong>CRITICAL:</strong> This method contains a <code>PESSIMISTIC_WRITE</code> lock.
     * Only use this for requests that strictly require transactional locking to prevent race conditions.
     * </p>
     *
     * @param customerKeycloakId The unique Keycloak ID of the customer.
     * @param purchaseCode       The unique code of the purchase.
     * @return An Optional containing the found Purchase, or empty if none matches.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    Optional<Purchase> getByCustomer_KeycloakIdAndPurchaseCode(String customerKeycloakId, String purchaseCode);

    Purchase getPurchasesByPurchaseCodeAndCustomer_KeycloakId(String purchaseCode, String customerKeycloakId);

    @Query(
"""
select p.id as id,
p.purchaseCode as purchaseCode,
p.contactName as recipientName,
p.contactNumber as recipientPhone,
p.address as deliveryAddress,
(case
    when p.email is null then c.email
    else p.email
end) as email,
p.date as purchaseDate,
p.totalCost as totalCost,
c.keycloakId as userId,
c.firstName as userName,
c.lastName as userFamilyName,
p.deliveryStatus as deliveryStatus
from Purchase p
left join p.customer c
order by
case
    when p.deliveryStatus= 'REFUND_REQUESTED' then 0
    else 1
end,
p.date desc
"""
    )
    Page<PurchaseProjection> getAllForAdminPaged(Pageable pageable);

    @Query(
"""
select count(p)
from Purchase p
where p.deliveryStatus = ?1
"""
    )
    Integer refundPendingCount(DeliveryStatus refundRequestedStatus);
}
