package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.SessionActivityProjection;
import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {

    @Query(value = """
select count (s) as total,
SUM(CASE WHEN s.isGuest = true THEN 1 ELSE 0 END) as guest,
SUM(CASE WHEN s.isGuest = false THEN 1 ELSE 0 END) as auth
from Session s
where s.isRevoked = false
and s.lastActivityAt>:fiveMinsAgo
and s.expiresAt >CURRENT_TIMESTAMP
""")
    SessionActivityProjection getActiveSessionCount(@Param("fiveMinsAgo")Instant fiveMinutesAgo);


    @Query(value =
"""
select s
from Session s
where s.expiresAt > CURRENT_TIMESTAMP
and s.isRevoked = false
and s.sessionId = :session_id
""")
    Optional<Session> getActiveById(@Param("session_id") String id);

    @Query(value =
            """
            select s
            from Session s
            where s.expiresAt <CURRENT_TIMESTAMP
            and s.isRevoked = false
            """)
    List<Session> getExpired();
//
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints({
//            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
//    })
//    Optional<Session> findBySessionId(String sessionId);

}
