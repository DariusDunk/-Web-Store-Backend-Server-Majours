package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> getCartOwnerByCustomer(Customer customer);

    Optional<Cart> getBySession(Session session);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
"""
delete
from
Cart c
where c.session.sessionId in ?1
""")
    void deleteBySessions(List<String> sessionIds);

//    @Query(
//"""
//select new com.example.ecomerseapplication.DTOs.responses.CartSummaryResponse(c.id.)
//from Cart c
//where c.session=?1
//""")
//    CartSummaryResponse getSummaryBySession(Session session);
}
