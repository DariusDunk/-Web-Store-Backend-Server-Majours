package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Auth.helpers.SessionExtractor;
import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
import com.example.ecomerseapplication.Repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.example.ecomerseapplication.Others.GlobalConstants.*;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CartService cartService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, CartService cartService) {
        this.sessionRepository = sessionRepository;
        this.cartService = cartService;
    }
//
//    public Session buildSession(Session session, ClientType clientType, boolean rememberMe, boolean isGuest) {
//
//        session.setIsGuest(isGuest);
//        session.setIsRevoked(false);
//        session.setClientType(clientType);
//        session.setIsRememberMeSession(rememberMe);
//
//        return sessionRepository.save(session);
//    }


//    public Session createAuthenticatedSession(String refreshToken, Customer customer, ClientType clientType, boolean rememberMe, int refreshTokenExpirySeconds) {
//        Session session = new Session();
//        session.setSessionId(generateSessionId());
//
//        if (rememberMe) {
//            session.setExpiresAt(Instant.now().plus(refreshTokenExpirySeconds, ChronoUnit.SECONDS));
//        } else {
//            session.setExpiresAt(Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS));
//        }
//
//        session.setCustomer(customer);
//        session.setRefreshToken(refreshToken);//todo tuk trqbva da se kriptira refresh tokena
//
//        return buildSession(session, clientType, rememberMe, false);
//    }

    public Session guestToLoginSession(Session session, KeycloakTokenResponse tokenResponse, boolean isRememberMe, Customer customer) {

        session.markAsAuthenticated(customer,
                tokenResponse.refreshToken(),
                isRememberMe,
                tokenResponse.refreshExpiresIn(), //todo tuk trqbva da se kriptira refresh tokena
                NORMAL_SESSION_TTL_HOURS);

        return sessionRepository.save(session);

//      return buildSession(session, session.getClientType(), isRememberMe, false);

    }

    private String generateSessionId() {
        byte[] random = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public void updateActivity(Session session) {

        boolean hasCart = cartService.existsBySession(session);
        session.registerActivity(hasCart, LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES, GUEST_SESSION_TTL_DAYS);

        sessionRepository.save(session);
    }

    public Session getById(String sessionId) {
        return sessionRepository.getActiveById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session expired or not found"));
    }

    public Optional<Session> getActiveByIdOptional(String sessionId) {
        return sessionRepository.getActiveById(sessionId);
    }

    public void save(Session session) {
        sessionRepository.save(session);
    }

    @Transactional
    public void revokeSessions(List<Session> expiredSessions) {

        for (Session session : expiredSessions) {
            session.revoke();
        }

        sessionRepository.saveAll(expiredSessions);

    }

    public List<Session> getExpiredSessions() {
        return sessionRepository.getExpired();
    }

    public Session createGuestSession(ClientType clientType) {

        Session session = new Session();
        session.setSessionId(generateSessionId());
        session.setClientType(clientType);
        session.markAsGuest(LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES);

        return sessionRepository.save(session);

//        session.setExpiresAt(Instant.now().plus(LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES, ChronoUnit.MINUTES));
//
//        return buildSession(session, clientType, false, true);
    }

    public Session AuthToGuestSession(Session session) {

        if (session.getIsGuest()) {
            throw new InvalidSessionException("Session is already a guest session");
        }

        session.markAsGuest(LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES);

//        session.setIsGuest(true);
//        session.setExpiresAt(Instant.now().plus(LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES, ChronoUnit.MINUTES));
//        session.setRefreshToken(null);
//        session.setCustomer(null);
//        session.setIsRememberMeSession(false);
//        session.setIsRevoked(false);

        return sessionRepository.save(session);
    }

    public Session getRequestSession() {
        return SessionExtractor.getRequestSession()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Session not found. SessionFilter should guarantee session presence."));
    }

    public static long calculateSessionTTLSeconds(Instant expiresAt) {
        return ChronoUnit.SECONDS.between(Instant.now(), expiresAt);
    }
}
