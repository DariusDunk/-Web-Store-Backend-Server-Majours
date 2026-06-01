package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.PurchaseCartId;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.ProductForDetailedPurchaseProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductPairProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductProjection;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Entities.PurchaseCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseCartRepository extends JpaRepository<PurchaseCart, PurchaseCartId> {

    @Query(value = "select pc " +
            "from PurchaseCart pc " +
            "left join fetch pc.purchaseCartId.product p " +
            "where pc.purchaseCartId.purchase=?1")
    List<PurchaseCart> getByPurchase(Purchase purchase);

    @Query("""
                select exists(select 1
                from PurchaseCart pc
                where pc.purchaseCartId.product.productCode = :productCode and pc.purchaseCartId.purchase.customer.keycloakId = :userId)
            """)
    Boolean isProductPurchased(@Param("productCode") String productCode, @Param("userId") String userId);

    @Query(
"""
select p.productCode as productCode,
p.productName as productName,
p.originalPriceStotinki as originalPriceStotinki,
s.discountPercent as defaultSaleDiscount,
sp.overrideDiscountPercentage explicitDiscount,
p.rating as rating,
p.mainImageUrl as imageUrl,
pc.quantity as quantity
from PurchaseCart pc
join pc.purchaseCartId.product p
join pc.purchaseCartId.purchase pu
left join p.saleProducts sp on sp.isMain = true
left join sp.sale s on s.isActive = true
and current_timestamp between s.startDate and s.endDate
where pu.purchaseCode = ?1
"""
    )
    List<PurchaseProductProjection> getProductProjectionsOfPurchase(String purchaseCode);

    @Query(
            """
select pur.id as purchaseId,
 p as purchaseProduct
from PurchaseCart pc
join pc.purchaseCartId.product p
join pc.purchaseCartId.purchase pur
where pur.id in :purchaseIds
order by pur.id desc, p.id
"""
    )
    List<PurchaseProductPairProjection> getProductsForCompactPurchaseHistory(@Param("purchaseIds") List<Long> purchaseIds);

    @Query(
"""
select pr.productCode as productCode,
pr.productName as productName,
pc.singlePrice as singlePrice,
pr.rating as rating,
pr.reviewCount as reviewCount,
pr.mainImageUrl as imageUrl,
pc.quantity as quantity
from PurchaseCart pc
join pc.purchaseCartId.purchase pu
join pc.purchaseCartId.product pr
where pu.purchaseCode = ?1
""")
    List<ProductForDetailedPurchaseProjection> getProductsForDetailedPurchaseHistory(String purchaseCode);

    @Query(
"""
select pc
from PurchaseCart pc
where pc.purchaseCartId.purchase.id = ?1
"""
    )
    List<PurchaseCart> getAllByPurchaseId(long purchaseId);
}
