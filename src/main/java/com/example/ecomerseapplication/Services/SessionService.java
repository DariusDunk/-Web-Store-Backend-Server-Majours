package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
import com.example.ecomerseapplication.Others.GlobalConstants;
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
import static com.example.ecomerseapplication.Others.GlobalConstants.GUEST_SESSION_TTL_DAYS;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CartProductService cartProductService;
    private final CartService cartService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, CartProductService cartProductService, CartService cartService) {
        this.sessionRepository = sessionRepository;
        this.cartProductService = cartProductService;
        this.cartService = cartService;
    }

    public Session buildSession(Session session, ClientType clientType, boolean rememberMe, boolean isGuest) {

        session.setIsGuest(isGuest);
        session.setIsRevoked(false);
        session.setClientType(clientType);
        session.setIsRememberMeSession(rememberMe);

        return sessionRepository.save(session);
    }

    public Session createAuthenticatedSession(String refreshToken, Customer customer, ClientType clientType, boolean rememberMe, int refreshTokenExpirySeconds) {
        Session session = new Session();
        session.setSessionId(generateSessionId());

        if (rememberMe) {
            session.setExpiresAt(Instant.now().plus(refreshTokenExpirySeconds, ChronoUnit.SECONDS));
        } else {
            session.setExpiresAt(Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS));
        }

        session.setCustomer(customer);
        session.setRefreshToken(refreshToken);//todo tuk trqbva da se kriptira refresh tokena

        return buildSession(session, clientType, rememberMe, false);
    }

    public Session guestToLoginSession(Session session, KeycloakTokenResponse tokenResponse, boolean isRememberMe, Customer customer) {

        if (isRememberMe) {
            session.setExpiresAt(Instant.now().plus(tokenResponse.refreshExpiresIn(), ChronoUnit.SECONDS));
        } else {
            session.setExpiresAt(Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS));
        }

        session.setCustomer(customer);
        session.setRefreshToken(tokenResponse.refreshToken());//todo tuk trqbva da se kriptira refresh tokena

      return buildSession(session, session.getClientType(), isRememberMe, false);

    }

    private String generateSessionId() {
        byte[] random = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public void updateActivity(String sessionId) {

        Session session = getById(sessionId);
        Instant now = Instant.now();

        if (now.isAfter(session.getLastActivityAt().plus(5, ChronoUnit.MINUTES)) && session.getIsRevoked() == false) {

//            System.out.println("Updating session activity timestamp");

            session.setLastActivityAt(now);
            if (session.getIsGuest()) {
               session.setExpiresAt(Instant.now().plus(GUEST_SESSION_TTL_DAYS, ChronoUnit.DAYS));
            }
            sessionRepository.save(session);
        }
    }

    public Session getById(String sessionId) {
        return sessionRepository.getActiveById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session expired or not found"));
    }

    public void save(Session session) {
        sessionRepository.save(session);
    }

    @Transactional
    public void revokeSessions(List<Session> expiredSessions) {

        for (Session session : expiredSessions) {
            session.setIsRevoked(true);
            session.setRevokedAt(Instant.now());
            session.setRefreshToken(null);
        }

        sessionRepository.saveAll(expiredSessions);
        System.out.println("Revoked " + expiredSessions.size() + " sessions");
        cartProductService.deleteItemsBySession(expiredSessions);
        System.out.println("Deleted cart products for revoked sessions");
        cartService.deleteCartsBySessions(expiredSessions);
        System.out.println("Deleted carts for revoked sessions");
    }

    //TODO dobavi logika za iztrivane na koli4ki ot iztekli sesii

    public List<Session> getExpiredSessions() {
        return sessionRepository.getExpired();
    }

    public Session createGuestSession(ClientType clientType) {

        Session session = new Session();
        session.setSessionId(generateSessionId());

        session.setExpiresAt(Instant.now().plus(GUEST_SESSION_TTL_DAYS, ChronoUnit.DAYS));

        return buildSession(session, clientType, false, true);
    }

    public Session LoginToGuestSession(String sessionId) {

        Session session = getById(sessionId);

        if (session.getIsGuest()) {
            throw new InvalidSessionException("Session is already a guest session");
        }

        session.setIsGuest(true);
        session.setExpiresAt(Instant.now().plus(GUEST_SESSION_TTL_DAYS, ChronoUnit.DAYS));
        session.setRefreshToken(null);
        session.setCustomer(null);
        session.setIsRememberMeSession(false);
        session.setIsRevoked(false);

        return sessionRepository.save(session);
    }
}
