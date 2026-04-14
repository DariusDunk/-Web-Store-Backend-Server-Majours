package com.example.ecomerseapplication.Utils;

import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.KeycloakService;
import com.example.ecomerseapplication.Services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SessionRevoker {

    private final SessionService sessionService;
    private final KeycloakService keycloakService;

    @Autowired
    public SessionRevoker(SessionService sessionService, KeycloakService keycloakService) {
        this.sessionService = sessionService;
        this.keycloakService = keycloakService;
    }

    public void revokeExpiredSessions() {

        List<Session> expiredSessions = sessionService.getExpiredSessions();
        if (!expiredSessions.isEmpty()) {
        sessionService.revokeSessions(expiredSessions);

        for (Session session : expiredSessions) {

            if (session.getRefreshToken()!=null
                    && session.getRefreshToken().isBlank()
                    &&!session.getIsGuest())
                keycloakService.invalidateRefreshToken(session.getRefreshToken());
        }
        System.out.println("Revoked refresh tokens for expired sessions.");

        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            System.out.println("-------------------------[" + now.format(formatter) + "]" + " Revoking expired sessions-------------------------");
            System.out.println("-------------------------Revoked " + expiredSessions.size() + " expired sessions-------------------------");
        }

    }

}
