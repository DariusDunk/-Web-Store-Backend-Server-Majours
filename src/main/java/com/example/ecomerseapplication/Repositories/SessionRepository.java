package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {
}
