package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final KeycloakService keycloakService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, KeycloakService keycloakService) {
        this.sessionRepository = sessionRepository;
        this.keycloakService = keycloakService;
    }

    public Session createSession(String refreshToken, Customer customer, ClientType clientType, boolean rememberMe) {
        Session session = new Session();

        session.setSessionId(generateSessionId());

//        System.out.println("session id: " + session.getSessionId() );

        session.setCustomer(customer);
        session.setExpiresAt(Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS));
        session.setRefreshToken(refreshToken);
        session.setIsGuest(false);// TODO tova 6te se promeni v byde6te
        session.setIsRevoked(false);
        session.setClientType(clientType);
        session.setRememberMeSession(rememberMe);

       return sessionRepository.save(session);

    }

    private String generateSessionId() {
        byte[] random = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public void revokeSession(Session session) {
        session.setIsRevoked(true);
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);
    }

    public Session getById(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }
}
