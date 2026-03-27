package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientTypeRepository extends JpaRepository<ClientType, Long> {
    Optional<ClientType> findClientTypeByClientTypeName(String clientTypeName);
}
