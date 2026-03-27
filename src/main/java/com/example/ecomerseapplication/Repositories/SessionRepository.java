package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {

    @Query(value = """
select count (s)
from Session s
where s.lastActivityAt<:fiveMinsAgo
""")
    int getActiveSessionCount(@Param("fiveMinsAgo") String timeThreshold);//Todo test/optimize
}
