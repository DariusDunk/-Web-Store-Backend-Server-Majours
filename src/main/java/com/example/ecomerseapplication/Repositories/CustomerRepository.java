package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);

    @Query(value = "select c.password " +
            "from Customer c " +
            "where c.email = ?1")
    char[] getPassword(String email);

    //    @Query(value = "select c.id " +
//            "from Customer c " +
//            "where c.email = ?1")
    Optional<Customer> getCustomerByEmail(String email);

    Optional<Customer> getByEmail(String email);

    @Query("""
            select c.customerPfp
            from Customer c
            where c.id=?1
            """)
    String getCustomerPfp(int customerId);
}
