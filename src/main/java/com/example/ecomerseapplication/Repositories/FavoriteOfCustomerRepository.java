package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.FavoriteOfCustomerId;
import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.FavoriteOfCustomer;
import com.example.ecomerseapplication.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteOfCustomerRepository extends JpaRepository<FavoriteOfCustomer, FavoriteOfCustomerId> {

    Integer countAllByFavoriteOfCustomerId_Customer_keycloakId(String keycloakId);

    Boolean existsByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(Customer customer, Product product);

    @Modifying
    void deleteFavoriteOfCustomerByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(Customer favoriteOfCustomerIdCustomer, Product favoriteOfCustomerIdProduct);

    @Query("""
            delete from FavoriteOfCustomer foc
            where foc.favoriteOfCustomerId.customer=:customer and foc.favoriteOfCustomerId.product.productCode in :productCodes
            """)
    @Modifying
    void deleteBatch(@Param("customer") Customer customer, @Param("productCodes") List<String> productCodes);

    @Query("""
            select new com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto(
                        foc.favoriteOfCustomerId.product.productCode,
                        foc.favoriteOfCustomerId.product.productName,
                        foc.favoriteOfCustomerId.product.originalPriceStotinki,
                        s.discountPercent,
                        sp.overrideDiscountPercentage,
                        foc.favoriteOfCustomerId.product.rating,
                        SIZE(foc.favoriteOfCustomerId.product.reviews),
                        foc.favoriteOfCustomerId.product.mainImageUrl,
                        case when foc.favoriteOfCustomerId.product.quantityInStock>0 then true else false end)
                        from FavoriteOfCustomer foc
                        left join foc.favoriteOfCustomerId.product.saleProducts sp
                        left join sp.sale s
                        with s.isActive = true
                        and current_timestamp between s.startDate and s.endDate
                        where foc.favoriteOfCustomerId.customer.keycloakId = :customerId
                        order by foc.dateAdded desc
            """)
    Page<CompactProductDto> getFromFavouritesPage(@Param("customerId") String customer, Pageable pageable);
}
