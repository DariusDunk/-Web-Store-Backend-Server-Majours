package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {

//    @Query(value = """
//select count (s)
//from Session s
//where s.lastActivityAt<:fiveMinsAgo
//""")
//    int getActiveSessionCount(@Param("fiveMinsAgo") String timeThreshold);//Todo test/optimize


    @Query(value =
"""
select s
from Session s
where s.expiresAt > CURRENT_TIMESTAMP
and s.isRevoked = false
and s.sessionId = :session_id
""")
    Optional<Session> getActiveById(@Param("session_id") String id);

    @Modifying
    @Query(value =
"""
update Session set
isRevoked = true,
revokedAt = current_timestamp
where expiresAt < CURRENT_TIMESTAMP
and isRevoked = false
""")
    int revokeExpired();

}
