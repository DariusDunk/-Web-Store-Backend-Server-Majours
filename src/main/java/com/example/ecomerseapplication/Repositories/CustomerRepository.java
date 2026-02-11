package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.Entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);

    Optional<Customer> getByEmail(String email);

    @Query("""
            select c.customerPfp
            from Customer c
            where c.id=?1
            """)
    String getCustomerPfp(int customerId);


    @Query("""
            select new com.example.ecomerseapplication.DTOs.responses.CompactProductResponse(
                        p.productCode,
                        p.productName,
                        p.originalPriceStotinki,
                        p.salePriceStotinki,
                        p.rating,
                        SIZE(p.reviews),
                        p.mainImageUrl,
                        case when p.quantityInStock>0 then true else false end)
                        from Product p
                        join p.favouredBy c
                        where c.keycloakId = :customerId
            """)//TODO zameni tazi zaqvka sys starata zaqvka sled kato priklu4is migriraneto
    Page<CompactProductResponse> getFromFavouritesPage(@Param("customerId") String  customer, Pageable pageable);


// @Query("""
//            select new com.example.ecomerseapplication.DTOs.responses.CompactProductResponse(
//                        p.productCode,
//                        p.productName,
//                        p.originalPriceStotinki,
//                        p.salePriceStotinki,
//                        p.rating,
//                        SIZE(p.reviews),
//                        p.mainImageUrl,
//                        case when p.quantityInStock>0 then true else false end)
//                        from Product p
//                         where :customer MEMBER OF p.favouredBy
//            """)
//    Page<CompactProductResponse> getFromFavouritesPage(@Param("customer") Customer customer, Pageable pageable);

    @Query("select c.id from Customer c where c.keycloakId = ?1")
    Long getIdByKeycloakId(String keycloakId);

    Customer getCustomerByKeycloakId(String keycloakId);
}
