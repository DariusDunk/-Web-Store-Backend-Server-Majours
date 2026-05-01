package com.example.ecomerseapplication.Utils.Revokers;

import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.CartProductService;
import com.example.ecomerseapplication.Services.CartService;
import com.example.ecomerseapplication.Services.KeycloakService;
import com.example.ecomerseapplication.Services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SessionRevoker {

    private final SessionService sessionService;
    private final KeycloakService keycloakService;
    private final CartProductService cartProductService;
    private final CartService cartService;

    @Autowired
    public SessionRevoker(SessionService sessionService, KeycloakService keycloakService, CartProductService cartProductService, CartService cartService) {
        this.sessionService = sessionService;
        this.keycloakService = keycloakService;
        this.cartProductService = cartProductService;
        this.cartService = cartService;
    }

    @Transactional
    public void revokeExpiredSessions() {

        List<Session> expiredSessions = sessionService.getExpiredSessions();
        if (!expiredSessions.isEmpty()) {
            sessionService.revokeSessions(expiredSessions);
            cartProductService.deleteItemsBySession(expiredSessions);
            cartService.deleteCartsBySessions(expiredSessions);

            boolean hasAuth = false;

            for (Session session : expiredSessions) {

                if (session.getRefreshToken() != null
                        && session.getRefreshToken().isBlank()
                        && !session.getIsGuest()) {
                    keycloakService.invalidateRefreshToken(session.getRefreshToken());
                    hasAuth = true;
                }
            }


            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            System.out.println("-------------------------[" + now.format(formatter) + "]" + " Revoking expired sessions-------------------------");
            if (hasAuth) System.out.println("Revoked refresh tokens for expired sessions.");
            System.out.println("-------------------------Revoked " + expiredSessions.size() + " expired sessions-------------------------");
        }

    }

}
