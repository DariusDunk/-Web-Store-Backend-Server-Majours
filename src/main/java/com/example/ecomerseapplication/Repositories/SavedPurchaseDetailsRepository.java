package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.SavedPurchaseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedPurchaseDetailsRepository extends JpaRepository<SavedPurchaseDetails, Long> {
//    Optional<SavedPurchaseDetails> getByCustomer(Customer customer); TODO vyrvni tova sled kato migraciqta priklu4i


//    @Query("""
//select spd
//from SavedPurchaseDetails spd
//where spd.customer.keycloakId =:keycloakId
//        """)
//    List<SavedPurchaseDetails> getByCustomer(@Param("keycloakId")String customer);


    @Query("""
select spd
from SavedPurchaseDetails spd
where spd.customer.keycloakId =:keycloakId
        """)
    Optional<SavedPurchaseDetails> getByCustomer(@Param("keycloakId")String customerId);
}
