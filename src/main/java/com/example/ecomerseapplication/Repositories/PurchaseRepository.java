package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Long> {

    @Query("""
        select p
                from Purchase p
                        where p.customer.keycloakId= :keycloakId
        """
            )
    List<Purchase> getByCustomer(@Param("keycloakId")String  customer);
    //TODO zameni noviq metod s tozi, kogato migraciqta priklu4i
//    @Query("""
//        select p
//                from Purchase p
//                        where p.customer.keycloakId=
//        """
//    )
//    List<Purchase> getByCustomer(Customer customer);


}
